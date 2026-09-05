package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.database.repositories.PlayerRepository
import me.kyleseven.pixelessentials.managers.TeleportPlan
import me.kyleseven.pixelessentials.managers.TeleportService
import me.kyleseven.pixelessentials.utils.mmd
import me.kyleseven.pixelessentials.utils.runTask
import me.kyleseven.pixelessentials.utils.runTaskAsync
import me.kyleseven.pixelessentials.utils.toLocation
import me.kyleseven.pixelessentials.utils.toPlayerHome
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class HomeCommands(
    private val plugin: PixelEssentials,
    private val repository: PlayerRepository,
    private val teleports: TeleportService
) : BaseCommand() {
    @CommandAlias("sethome")
    @Description("Set your home location")
    @CommandPermission("pixelessentials.sethome")
    fun onSethome(player: Player) {
        val playerId = player.uniqueId
        val home = player.location.toPlayerHome()
        runTaskAsync(plugin) {
            repository.upsertPlayerHome(playerId, home)
            runTask(plugin) {
                player.sendMessage(mmd("<gray>Your home location has been set.</gray>"))
            }
        }
    }

    @CommandAlias("delhome")
    @Description("Delete your home location")
    @CommandPermission("pixelessentials.delhome")
    fun onDelhome(player: Player) {
        val playerId = player.uniqueId
        runTaskAsync(plugin) {
            val deleted = repository.getPlayerHome(playerId) != null
            if (deleted) repository.deletePlayerHome(playerId)

            runTask(plugin) {
                if (deleted) {
                    player.sendMessage(mmd("<gray>Your home location has been deleted.</gray>"))
                } else {
                    player.sendMessage(mmd("<red>You don't have a home location set.</red>"))
                }
            }
        }
    }

    @CommandAlias("home")
    @Description("Teleport to your home location")
    @CommandPermission("pixelessentials.home")
    fun onHome(player: Player) {
        if (teleports.denyIfOnCooldown(player)) return
        val playerId = player.uniqueId

        runTaskAsync(plugin) {
            val home = repository.getPlayerHome(playerId)
            runTask(plugin) mainThread@{
                if (home == null) {
                    player.sendMessage(mmd("<red>You don't have a home location set.</red>"))
                    return@mainThread
                }
                val world = Bukkit.getWorld(home.world)
                if (world == null) {
                    player.sendMessage(mmd("<red>World <white>${home.world}</white> does not exist.</red>"))
                    return@mainThread
                }

                player.report(
                    teleports.schedule(
                        TeleportPlan(player, { home.toLocation(world) }, "home")
                    )
                )
            }
        }
    }
}
