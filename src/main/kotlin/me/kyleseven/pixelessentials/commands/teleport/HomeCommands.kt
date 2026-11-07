package me.kyleseven.pixelessentials.commands.teleport

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.database.models.toStoredLocation
import me.kyleseven.pixelessentials.utils.mmd
import me.kyleseven.pixelessentials.utils.runTask
import me.kyleseven.pixelessentials.utils.runTaskAsync
import org.bukkit.entity.Player

class HomeCommands(private val plugin: PixelEssentials) : BaseCommand() {
    @CommandAlias("home")
    @Description("Teleport to your home location")
    @CommandPermission("pixelessentials.home")
    fun onHome(player: Player) {
        if (rejectIfOnCooldown(plugin, player)) return

        teleportToStored(plugin, player, "home", mmd("<red>You don't have a home location set.</red>")) {
            plugin.playerRepository.getPlayerHome(player.uniqueId)
        }
    }

    @CommandAlias("sethome")
    @Description("Set your home location")
    @CommandPermission("pixelessentials.sethome")
    fun onSethome(player: Player) {
        val home = player.location.toStoredLocation()

        runTaskAsync(plugin) {
            plugin.playerRepository.upsertPlayerHome(player.uniqueId, home)

            runTask(plugin) {
                player.sendMessage(mmd("<gray>Your home location has been set.</gray>"))
            }
        }
    }

    @CommandAlias("delhome")
    @Description("Delete your home location")
    @CommandPermission("pixelessentials.delhome")
    fun onDelhome(player: Player) {
        runTaskAsync(plugin) {
            plugin.playerRepository.getPlayerHome(player.uniqueId) ?: run {
                runTask(plugin) {
                    player.sendMessage(mmd("<red>You don't have a home location set.</red>"))
                }
                return@runTaskAsync
            }

            plugin.playerRepository.deletePlayerHome(player.uniqueId)

            runTask(plugin) {
                player.sendMessage(mmd("<gray>Your home location has been deleted.</gray>"))
            }
        }
    }
}