package me.kyleseven.pixelessentials.utils

import me.kyleseven.pixelessentials.PixelEssentials
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PlaytimeTracker(private val plugin: PixelEssentials) {
    private val sessionStartTimes = ConcurrentHashMap<UUID, Long>()

    private val updateIntervalTicks = 6000L
    private var taskId: Int = -1

    /**
     * Start tracking a player's session
     */
    fun startSession(uuid: UUID) {
        val currentTimestamp = System.currentTimeMillis() / 1000
        sessionStartTimes[uuid] = currentTimestamp
    }

    /**
     * End a player's session and return the session duration
     */
    fun endSession(uuid: UUID): Long {
        val currentTimestamp = System.currentTimeMillis() / 1000
        val startTime = sessionStartTimes.remove(uuid) ?: return 0

        // Don't count time if player is AFK
        return if (plugin.afkManager.isAfk(uuid)) {
            0
        } else {
            currentTimestamp - startTime
        }
    }

    /**
     * Initialize the playtime tracker
     */
    fun initialize() {
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            updatePlaytimes()
        }, updateIntervalTicks, updateIntervalTicks).taskId
    }

    /**
     * Shutdown the playtime tracker
     */
    fun shutdown() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId)
        }

        // Process final playtimes for all online players
        sessionStartTimes.keys.forEach { uuid ->
            val finalPlaytime = endSession(uuid)
            if (finalPlaytime > 0) {
                plugin.playerRepository.updateLastSeenAndPlaytime(uuid, finalPlaytime)
            }
        }

        sessionStartTimes.clear()
    }

    /**
     * Update all player playtimes in the database
     */
    private fun updatePlaytimes() {
        val currentTimestamp = System.currentTimeMillis() / 1000
        val toUpdate = HashMap<UUID, Long>()

        sessionStartTimes.forEach { (uuid, startTime) ->
            // Skip AFK players, just update their session start time
            if (plugin.afkManager.isAfk(uuid)) {
                sessionStartTimes[uuid] = currentTimestamp
                return@forEach
            }

            val elapsedTime = currentTimestamp - startTime
            if (elapsedTime > 0) {
                toUpdate[uuid] = elapsedTime
                sessionStartTimes[uuid] = currentTimestamp
            }
        }

        toUpdate.forEach { (uuid, playtime) ->
            plugin.playerRepository.updateLastSeenAndPlaytime(uuid, playtime)
        }
    }

    /**
     * Handle a player coming back from AFK
     */
    fun handleAfkReturn(uuid: UUID) {
        // Reset session start time for the player
        startSession(uuid)
    }
}