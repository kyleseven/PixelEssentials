package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Single
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.database.models.Warp
import me.kyleseven.pixelessentials.database.repositories.WarpRepository
import me.kyleseven.pixelessentials.managers.TeleportPlan
import me.kyleseven.pixelessentials.managers.TeleportService
import me.kyleseven.pixelessentials.utils.mmd
import me.kyleseven.pixelessentials.utils.runTask
import me.kyleseven.pixelessentials.utils.runTaskAsync
import me.kyleseven.pixelessentials.utils.toLocation
import me.kyleseven.pixelessentials.utils.toWarp
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WarpCommands(
    private val plugin: PixelEssentials,
    private val repository: WarpRepository,
    private val teleports: TeleportService
) : BaseCommand() {
    @CommandAlias("setwarp")
    @Description("Set a warp location")
    @CommandPermission("pixelessentials.setwarp")
    fun onSetwarp(player: Player, @Single name: String) {
        if (name.length > MAX_WARP_NAME_LENGTH) {
            player.sendMessage(mmd("<red>Warp location name must be 32 characters or less.</red>"))
            return
        }

        val warp = player.location.toWarp(name)
        val playerId = player.uniqueId
        runTaskAsync(plugin) {
            val alreadyExists = repository.getWarp(name) != null
            if (!alreadyExists) repository.upsertWarp(warp, playerId)

            runTask(plugin) {
                if (alreadyExists) {
                    player.sendMessage(mmd("<red>Warp location <white>$name</white> already exists.</red>"))
                } else {
                    player.sendMessage(mmd("<gray>Warp location <white>$name</white> has been set.</gray>"))
                }
            }
        }
    }

    @CommandAlias("delwarp")
    @Description("Delete a warp location")
    @CommandPermission("pixelessentials.delwarp")
    fun onDelwarp(player: Player, @Single name: String) {
        runTaskAsync(plugin) {
            val deleted = repository.getWarp(name) != null
            if (deleted) repository.deleteWarp(name)

            runTask(plugin) {
                if (deleted) {
                    player.sendMessage(mmd("<gray>Warp location <white>$name</white> has been deleted.</gray>"))
                } else {
                    player.sendMessage(mmd("<red>Warp location <white>$name</white> does not exist.</red>"))
                }
            }
        }
    }

    @CommandAlias("warp")
    @Description("Teleport to a warp location or list all warps")
    @CommandPermission("pixelessentials.warp")
    fun onWarp(sender: CommandSender, @Optional @Single name: String?) {
        if (name.isNullOrBlank() || sender !is Player) {
            listWarps(sender)
            return
        }
        teleportToWarp(sender, name)
    }

    private fun listWarps(sender: CommandSender) {
        if (!sender.hasPermission("pixelessentials.warp.list")) {
            sender.sendMessage(mmd("<red>You don't have permission to list warps.</red>"))
            return
        }

        runTaskAsync(plugin) {
            val warps = repository.getWarps()
            val component = buildWarpList(warps)
            runTask(plugin) {
                if (warps.isEmpty()) {
                    sender.sendMessage(mmd("<red>There are no warps.</red>"))
                } else {
                    sender.sendMessage(component)
                }
            }
        }
    }

    private fun teleportToWarp(player: Player, name: String) {
        if (teleports.denyIfOnCooldown(player)) return

        runTaskAsync(plugin) {
            val warp = repository.getWarp(name)
            runTask(plugin) mainThread@{
                if (warp == null) {
                    player.sendMessage(mmd("<red>Warp location <white>$name</white> does not exist.</red>"))
                    return@mainThread
                }
                val world = Bukkit.getWorld(warp.world)
                if (world == null) {
                    player.sendMessage(mmd("<red>World <white>${warp.world}</white> does not exist.</red>"))
                    return@mainThread
                }

                player.report(
                    teleports.schedule(
                        TeleportPlan(player, { warp.toLocation(world) }, warp.name)
                    )
                )
            }
        }
    }

    private fun buildWarpList(warps: List<Warp>): Component = warps.foldIndexed(
        Component.text("Warps (${warps.size}): ", NamedTextColor.GRAY)
    ) { index, component, warp ->
        val command = "/warp ${warp.name}"
        val hoverText = Component.text(command, NamedTextColor.WHITE)
            .append(Component.newline())
            .append(Component.text("x: ", NamedTextColor.GRAY))
            .append(Component.text("%.1f".format(warp.x), NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text("y: ", NamedTextColor.GRAY))
            .append(Component.text("%.1f".format(warp.y), NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text("z: ", NamedTextColor.GRAY))
            .append(Component.text("%.1f".format(warp.z), NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text("world: ", NamedTextColor.GRAY))
            .append(Component.text(warp.world, NamedTextColor.WHITE))

        component
            .append(
                Component.text(warp.name, NamedTextColor.WHITE)
                    .clickEvent(ClickEvent.runCommand(command))
                    .hoverEvent(HoverEvent.showText(hoverText))
            )
            .let {
                if (index < warps.lastIndex) it.append(Component.text(", ", NamedTextColor.GRAY)) else it
            }
    }

    private companion object {
        const val MAX_WARP_NAME_LENGTH = 32
    }
}
