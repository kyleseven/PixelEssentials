package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.database.repositories.SpawnRepository
import me.kyleseven.pixelessentials.managers.TeleportPlan
import me.kyleseven.pixelessentials.managers.TeleportService
import me.kyleseven.pixelessentials.utils.mmd
import me.kyleseven.pixelessentials.utils.runTask
import me.kyleseven.pixelessentials.utils.runTaskAsync
import me.kyleseven.pixelessentials.utils.toLocation
import me.kyleseven.pixelessentials.utils.toSpawn
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class SpawnCommands(
    private val plugin: PixelEssentials,
    private val repository: SpawnRepository,
    private val teleports: TeleportService
) : BaseCommand() {
    @CommandAlias("spawn")
    @Description("Teleport to spawn")
    @CommandPermission("pixelessentials.spawn")
    fun onSpawn(player: Player) {
        if (teleports.denyIfOnCooldown(player)) return

        runTaskAsync(plugin) {
            val spawn = repository.getSpawn()
            runTask(plugin) mainThread@{
                if (spawn == null) {
                    player.sendMessage(mmd("<red>Spawn location has not been set.</red>"))
                    return@mainThread
                }
                val world = Bukkit.getWorld(spawn.world)
                if (world == null) {
                    player.sendMessage(mmd("<red>World <white>${spawn.world}</white> does not exist.</red>"))
                    return@mainThread
                }

                player.report(
                    teleports.schedule(
                        TeleportPlan(player, { spawn.toLocation(world) }, "spawn")
                    )
                )
            }
        }
    }

    @CommandAlias("setspawn")
    @Description("Set the spawn location")
    @CommandPermission("pixelessentials.setspawn")
    fun onSetspawn(player: Player) {
        val spawn = player.location.toSpawn()
        runTaskAsync(plugin) {
            repository.upsertSpawn(spawn)
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
            val deleted = repository.getSpawn() != null
            if (deleted) repository.deleteSpawn()

            runTask(plugin) {
                if (deleted) {
                    player.sendMessage(mmd("<gray>Spawn location has been deleted.</gray>"))
                } else {
                    player.sendMessage(mmd("<red>Spawn location has not been set.</red>"))
                }
            }
        }
    }
}
