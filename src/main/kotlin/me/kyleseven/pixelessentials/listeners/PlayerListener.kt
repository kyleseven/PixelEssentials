package me.kyleseven.pixelessentials.listeners

import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.database.models.Player
import me.kyleseven.pixelessentials.database.models.PlayerLastLocation
import me.kyleseven.pixelessentials.utils.mmd
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PlayerListener(private val plugin: PixelEssentials) : Listener {
    private val sessionStartTimes = ConcurrentHashMap<UUID, Int>()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val ipAddress = player.address.address.hostAddress

        // Save and suppress original message
        val joinMessage = event.joinMessage()
        event.joinMessage(null)

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val existingPlayer = plugin.playerRepository.getPlayer(uuid)
            val isNewPlayer = existingPlayer == null

            val currentTimestamp = (System.currentTimeMillis() / 1000).toInt()
            sessionStartTimes[uuid] = currentTimestamp

            val dbPlayer = existingPlayer ?: Player(
                lastAccountName = player.name,
                uuid = uuid.toString(),
                ipAddress = ipAddress,
                firstJoin = currentTimestamp,
                lastSeen = currentTimestamp,
                totalPlaytime = 0,
                isBanned = false,
                banReason = null
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val player = event.player
            if (player.isBanned) {
                plugin.playerRepository.updateBanStatus(player.uniqueId, true, "")
            }

            val currentTimestamp = (System.currentTimeMillis() / 1000).toInt()
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
}