package me.kyleseven.pixelessentials.listeners

import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.database.models.Player
import me.kyleseven.pixelessentials.database.models.PlayerLastLocation
import me.kyleseven.pixelessentials.utils.mmd
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PlayerListener(private val plugin: PixelEssentials) : Listener {
    private val sessionStartTimes = ConcurrentHashMap<UUID, Long>()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val ipAddress = player.address.address.hostAddress

        // Get custom join message (if enabled) and suppress default join message
        val joinMessage: Component? = if (plugin.configProvider.customJoinMessageEnabled) {
            mmd(plugin.configProvider.customJoinMessage.replace("{username}", player.name))
        } else {
            event.joinMessage()
        }
        event.joinMessage(null)

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val existingPlayer = plugin.playerRepository.getPlayer(uuid)
            val isNewPlayer = existingPlayer == null

            val currentTimestamp = (System.currentTimeMillis() / 1000)
            sessionStartTimes[uuid] = currentTimestamp

            val dbPlayer = existingPlayer ?: Player(
                lastAccountName = player.name,
                uuid = uuid.toString(),
                ipAddress = ipAddress,
                firstJoin = currentTimestamp,
                lastSeen = currentTimestamp,
                totalPlaytime = 0
            )

            val updatedPlayer = dbPlayer.copy(
                lastAccountName = player.name,
                ipAddress = ipAddress,
                lastSeen = currentTimestamp
            )

            plugin.playerRepository.upsertPlayer(updatedPlayer)

            // Broadcast welcome message
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (isNewPlayer && plugin.configProvider.welcomeMessageEnabled) {
                    val welcomeMessage = plugin.configProvider.welcomeMessage.replace("{username}", player.name)
                    plugin.server.broadcast(mmd(welcomeMessage))
                }

                if (joinMessage != null) {
                    plugin.server.broadcast(joinMessage)
                }

                if (plugin.configProvider.motdShowOnJoin) {
                    player.sendMessage(plugin.motdBuilder.build(player))
                }
            })
        })
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (plugin.configProvider.customLeaveMessageEnabled) {
            event.quitMessage(mmd(plugin.configProvider.customLeaveMessage.replace("{username}", event.player.name)))
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val player = event.player

            val currentTimestamp = (System.currentTimeMillis() / 1000)
            val sessionTime = (currentTimestamp - (sessionStartTimes[player.uniqueId] ?: currentTimestamp))

            plugin.playerRepository.updateLastSeenAndPlaytime(player.uniqueId, sessionTime)
            plugin.playerRepository.upsertPlayerLastLocation(
                player.uniqueId, PlayerLastLocation(
                    x = player.location.x,
                    y = player.location.y,
                    z = player.location.z,
                    pitch = player.location.pitch.toDouble(),
                    yaw = player.location.yaw.toDouble(),
                    world = player.world.name
                )
            )
        })
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (event.isCancelled) return

        if (event.player.hasPermission("pixelessentials.back.ondeath")) {
            plugin.teleportManager.recordBackLocation(event.player, event.entity.location.clone())
        }
    }
}