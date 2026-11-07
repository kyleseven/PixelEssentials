package me.kyleseven.pixelessentials.commands.teleport

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.database.models.Warp
import me.kyleseven.pixelessentials.database.models.toStoredLocation
import me.kyleseven.pixelessentials.utils.mmd
import me.kyleseven.pixelessentials.utils.runTask
import me.kyleseven.pixelessentials.utils.runTaskAsync
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WarpCommands(private val plugin: PixelEssentials) : BaseCommand() {
    private val maxNameLength = 32

    @CommandAlias("warp")
    @Description("Teleport to a warp location or list all warps")
    @CommandPermission("pixelessentials.warp")
    fun onWarp(sender: CommandSender, @Optional @Single name: String?) {
        // List all warp locations if name is not provided or sender is not a player
        if (name.isNullOrBlank() || sender !is Player) {
            listWarps(sender)
            return
        }

        if (rejectIfOnCooldown(plugin, sender)) return

        runTaskAsync(plugin) {
            val warp = plugin.warpRepository.getWarp(name)

            runTask(plugin) {
                if (warp == null) {
                    sender.sendMessage(
                        mmd("<red>Warp location <white><name></white> does not exist.</red>", Placeholder.unparsed("name", name))
                    )
                    return@runTask
                }

                // Use the stored name so the messages match its saved capitalization
                teleportToStored(plugin, sender, warp.name, warp.location)
            }
        }
    }

    @CommandAlias("setwarp")
    @Description("Set a warp location")
    @CommandPermission("pixelessentials.setwarp")
    fun onSetwarp(player: Player, @Single name: String) {
        if (name.length > maxNameLength) {
            player.sendMessage(mmd("<red>Warp location name must be $maxNameLength characters or less.</red>"))
            return
        }

        val location = player.location.toStoredLocation()

        runTaskAsync(plugin) {
            val existing = plugin.warpRepository.getWarp(name)
            if (existing != null) {
                runTask(plugin) {
                    player.sendMessage(
                        mmd("<red>Warp location <white><name></white> already exists.</red>", Placeholder.unparsed("name", existing.name))
                    )
                }
                return@runTaskAsync
            }

            plugin.warpRepository.upsertWarp(Warp(name, location), player.uniqueId)

            runTask(plugin) {
                player.sendMessage(
                    mmd("<gray>Warp location <white><name></white> has been set.</gray>", Placeholder.unparsed("name", name))
                )
            }
        }
    }

    @CommandAlias("delwarp")
    @Description("Delete a warp location")
    @CommandPermission("pixelessentials.delwarp")
    fun onDelwarp(player: Player, @Single name: String) {
        runTaskAsync(plugin) {
            plugin.warpRepository.getWarp(name) ?: run {
                runTask(plugin) {
                    player.sendMessage(
                        mmd("<red>Warp location <white><name></white> does not exist.</red>", Placeholder.unparsed("name", name))
                    )
                }
                return@runTaskAsync
            }

            plugin.warpRepository.deleteWarp(name)

            runTask(plugin) {
                player.sendMessage(
                    mmd("<gray>Warp location <white><name></white> has been deleted.</gray>", Placeholder.unparsed("name", name))
                )
            }
        }
    }

    private fun listWarps(sender: CommandSender) {
        if (!sender.hasPermission("pixelessentials.warp.list")) {
            sender.sendMessage(mmd("<red>You don't have permission to list warps.</red>"))
            return
        }

        runTaskAsync(plugin) {
            val warps = plugin.warpRepository.getWarps()

            runTask(plugin) {
                if (warps.isEmpty()) {
                    sender.sendMessage(mmd("<red>There are no warps.</red>"))
                } else {
                    sender.sendMessage(buildWarpList(warps))
                }
            }
        }
    }

    private fun buildWarpList(warps: List<Warp>): Component {
        val entries = warps.map { warp ->
            val warpCommand = "/warp ${warp.name}"
            val location = warp.location
            val hoverText = Component.text()
                .append(Component.text(warpCommand, NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("x: ", NamedTextColor.GRAY))
                .append(Component.text("%.1f".format(location.x), NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("y: ", NamedTextColor.GRAY))
                .append(Component.text("%.1f".format(location.y), NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("z: ", NamedTextColor.GRAY))
                .append(Component.text("%.1f".format(location.z), NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("world: ", NamedTextColor.GRAY))
                .append(Component.text(location.world, NamedTextColor.WHITE))
                .build()

            Component.text(warp.name, NamedTextColor.WHITE)
                .clickEvent(ClickEvent.runCommand(warpCommand))
                .hoverEvent(HoverEvent.showText(hoverText))
        }

        return Component.text()
            .append(Component.text("Warps (${warps.size}): ", NamedTextColor.GRAY))
            .append(Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)), entries))
            .build()
    }
}