package me.kyleseven.pixelessentials.managers

import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.utils.mmd
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AFKManager(private val plugin: PixelEssentials) {
    private val afkPlayers = ConcurrentHashMap<UUID, Long>()
    private val lastActivity = ConcurrentHashMap<UUID, Long>()

    private val checkIntervalTicks = 1200L
    private var taskId: Int = -1

    /**
     * Initialize the AFK manager
     */
    fun initialize() {
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            checkAfkPlayers()
        }, checkIntervalTicks, checkIntervalTicks).taskId
    }

    /**
     * Shutdown and cleanup
     */
    fun shutdown() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId)
        }
        afkPlayers.clear()
        lastActivity.clear()
    }

    /**
     * Check if a player is AFK
     */
    fun isAfk(player: Player): Boolean {
        return afkPlayers.containsKey(player.uniqueId)
    }

    fun isAfk(uuid: UUID): Boolean {
        return afkPlayers.containsKey(uuid)
    }

    /**
     * Set a player's AFK status
     */
    fun setAfk(player: Player, isAfk: Boolean) {
        val uuid = player.uniqueId

        if (isAfk && !afkPlayers.containsKey(uuid)) {
            // Player is going AFK
            afkPlayers[uuid] = System.currentTimeMillis() / 1000
            broadcastAfkStatus(player, true)
        } else if (!isAfk && afkPlayers.containsKey(uuid)) {
            // Player is no longer AFK
            afkPlayers.remove(uuid)
            broadcastAfkStatus(player, false)

            lastActivity[uuid] = System.currentTimeMillis() / 1000

            plugin.playtimeTracker.handleAfkReturn(uuid)
        }
    }

    /**
     * Update a player's activity timestamp and remove AFK status if they were AFK
     */
    fun updateActivity(player: Player) {
        val currentTime = System.currentTimeMillis() / 1000
        lastActivity[player.uniqueId] = currentTime

        // If player was AFK, mark them as not AFK
        if (isAfk(player)) {
            setAfk(player, false)
        }
    }

    /**
     * Check for players who have been inactive and mark them as AFK
     */
    private fun checkAfkPlayers() {
        val currentTime = System.currentTimeMillis() / 1000

        Bukkit.getOnlinePlayers().forEach { player ->
            val uuid = player.uniqueId

            // Skip if player is already AFK
            if (afkPlayers.containsKey(uuid)) {
                return@forEach
            }

            val lastActiveTime = lastActivity.getOrDefault(uuid, currentTime)
            val idleTime = currentTime - lastActiveTime

            // If player has been idle for too long, mark them AFK
            if (idleTime >= plugin.configProvider.afkTimeout) {
                setAfk(player, true)
            }
        }
    }

    private fun broadcastAfkStatus(player: Player, isAfk: Boolean) {
        val message = if (isAfk) {
            "<gray>${player.name} is now AFK.</gray>"
        } else {
            "<gray>${player.name} is no longer AFK.</gray>"
        }

        plugin.server.broadcast(mmd(message))
    }

    /**
     * Remove a player from AFK tracking
     */
    fun removePlayer(uuid: UUID) {
        afkPlayers.remove(uuid)
        lastActivity.remove(uuid)
    }

    /**
     * Handle a new player joining
     */
    fun handlePlayerJoin(uuid: UUID) {
        val currentTime = System.currentTimeMillis() / 1000
        lastActivity[uuid] = currentTime
    }
}