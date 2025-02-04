package me.kyleseven.pixelessentials.listeners

import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.database.models.Player
import me.kyleseven.pixelessentials.utils.mmd
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PlayerListener(private val plugin: PixelEssentials) : Listener {
    private val sessionStartTimes = ConcurrentHashMap<UUID, Int>()

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val ipAddress = player.address.address.hostAddress

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
            if (isNewPlayer && plugin.configProvider.welcomeMessageEnabled) {
                val welcomeMessage = plugin.configProvider.welcomeMessage.replace("{username}", player.name)

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    plugin.server.broadcast(mmd(welcomeMessage))
                })
            }
        })
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val player = e.player
            if (player.isBanned) {
                plugin.playerRepository.updateBanStatus(player.uniqueId, true, "")
            }

            val sessionTime = ((System.currentTimeMillis() / 1000).toInt() - sessionStartTimes[player.uniqueId]!!)

            plugin.playerRepository.updateLastSeenAndPlaytime(player.uniqueId, sessionTime)
        })
    }
}