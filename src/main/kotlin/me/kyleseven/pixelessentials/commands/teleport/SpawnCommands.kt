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

class SpawnCommands(private val plugin: PixelEssentials) : BaseCommand() {
    @CommandAlias("spawn")
    @Description("Teleport to spawn")
    @CommandPermission("pixelessentials.spawn")
    fun onSpawn(player: Player) {
        if (rejectIfOnCooldown(plugin, player)) return

        teleportToStored(plugin, player, "spawn", mmd("<red>Spawn location has not been set.</red>")) {
            plugin.spawnRepository.getSpawn()
        }
    }

    @CommandAlias("setspawn")
    @Description("Set the spawn location")
    @CommandPermission("pixelessentials.setspawn")
    fun onSetspawn(player: Player) {
        val spawn = player.location.toStoredLocation()

        runTaskAsync(plugin) {
            plugin.spawnRepository.upsertSpawn(spawn)

            runTask(plugin) {
                player.sendMessage(mmd("<gray>Spawn location has been set.</gray>"))
            }
        }
    }

    @CommandAlias("delspawn")
    @Description("Delete the spawn location")
    @CommandPermission("pixelessentials.delspawn")
    fun onDelspawn(player: Player) {
        runTaskAsync(plugin) {
            plugin.spawnRepository.getSpawn() ?: run {
                runTask(plugin) {
                    player.sendMessage(mmd("<red>Spawn location has not been set.</red>"))
                }
                return@runTaskAsync
            }

            plugin.spawnRepository.deleteSpawn()

            runTask(plugin) {
                player.sendMessage(mmd("<gray>Spawn location has been deleted.</gray>"))
            }
        }
    }
}