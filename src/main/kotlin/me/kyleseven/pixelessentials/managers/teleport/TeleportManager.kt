package me.kyleseven.pixelessentials.managers.teleport

import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.utils.mmd
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.math.abs

/**
 * Runs teleports: the warmup countdown, the teleport itself, cooldowns, and /back locations.
 */
class TeleportManager(private val plugin: PixelEssentials) {
    private val warmups = mutableMapOf<UUID, Warmup>()
    private val cooldowns = mutableMapOf<UUID, Long>()
    private val backLocations = mutableMapOf<UUID, Location>()

    /**
     * Get the seconds left on a player's teleport cooldown, rounded up.
     *
     * @return The remaining seconds, or 0 if the player is not on cooldown.
     */
    fun remainingCooldown(player: Player): Long {
        val remainingMillis = (cooldowns[player.uniqueId] ?: 0) - System.currentTimeMillis()
        return if (remainingMillis > 0) (remainingMillis + 999) / 1000 else 0
    }

    /**
     * Set the location a player returns to with /back.
     */
    fun recordBackLocation(player: Player, location: Location) {
        backLocations[player.uniqueId] = location.clone()
    }

    fun getBackLocation(player: Player): Location? {
        return backLocations[player.uniqueId]
    }

    /**
     * Start a teleport, replacing any teleport already in progress for the same player.
     *
     * @param teleport The teleport to start.
     * @param delaySeconds Warmup during which the player must not move or take damage.
     * @param applyCooldown Whether to put the player on cooldown once the teleport completes.
     */
    fun start(
        teleport: Teleport,
        delaySeconds: Int = plugin.configProvider.teleportDelay,
        applyCooldown: Boolean = true
    ) {
        val player = teleport.player
        abort(player, CancelReason.SUPERSEDED)

        if (delaySeconds <= 0 || player.hasPermission("pixelessentials.teleport.delay.bypass")) {
            complete(teleport, player.location.clone(), applyCooldown)
            return
        }

        warmups[player.uniqueId] = Warmup(teleport, delaySeconds, applyCooldown).also {
            it.runTaskTimer(plugin, 20L, 5L)
        }
        player.sendMessage(
            mmd(
                "<gray>Teleporting to</gray> <white><destination></white> <gray>in $delaySeconds seconds. Do not move.</gray>",
                Placeholder.component("destination", teleport.destinationName)
            )
        )
    }

    private fun abort(player: Player, reason: CancelReason) {
        val warmup = warmups.remove(player.uniqueId) ?: return
        warmup.cancel()
        notifyCanceled(warmup.teleport, reason)
    }

    private fun complete(teleport: Teleport, origin: Location, applyCooldown: Boolean) {
        val player = teleport.player
        val destination = teleport.destination() ?: run {
            notifyCanceled(teleport, CancelReason.DESTINATION_UNAVAILABLE)
            return
        }

        // Another plugin may cancel the teleport event
        if (!player.teleport(destination)) {
            player.sendMessage(mmd("<red>Teleport failed.</red>"))
            return
        }

        if (player.hasPermission("pixelessentials.back.onteleport")) {
            recordBackLocation(player, origin)
        }
        if (applyCooldown && !player.hasPermission("pixelessentials.teleport.cooldown.bypass")) {
            cooldowns[player.uniqueId] = System.currentTimeMillis() + plugin.configProvider.teleportCooldown * 1000L
        }
        player.sendMessage(
            mmd(
                "<gray>Teleported to</gray> <white><destination></white><gray>.</gray>",
                Placeholder.component("destination", teleport.destinationName)
            )
        )
    }

    private fun notifyCanceled(teleport: Teleport, reason: CancelReason) {
        val player = teleport.player
        val playerMessage = when (reason) {
            CancelReason.LOGGED_OFF -> null
            CancelReason.MOVED -> "<red>Teleport canceled because you moved.</red>"
            CancelReason.DAMAGED -> "<red>Teleport canceled because you took damage.</red>"
            CancelReason.SUPERSEDED -> "<red>Previous teleport canceled due to new teleport request.</red>"
            CancelReason.DESTINATION_UNAVAILABLE -> "<red>Teleport canceled because the destination is no longer available.</red>"
        }
        if (playerMessage != null && player.isOnline) {
            player.sendMessage(mmd(playerMessage))
        }

        val because = when (reason) {
            CancelReason.LOGGED_OFF -> "they logged off"
            CancelReason.MOVED -> "they moved"
            CancelReason.DAMAGED -> "they took damage"
            CancelReason.SUPERSEDED -> "they started another teleport"
            // The observer is the destination, so they are the one who went away
            CancelReason.DESTINATION_UNAVAILABLE -> return
        }
        teleport.observer?.let { Bukkit.getPlayer(it) }?.sendMessage(
            mmd(
                "<red>Teleport from <player> was canceled because $because.</red>",
                Placeholder.component("player", player.displayName())
            )
        )
    }

    private fun hasMoved(from: Location, to: Location): Boolean {
        if (from.world != to.world) return true

        val tolerance = 0.5
        return abs(from.x - to.x) > tolerance ||
                abs(from.y - to.y) > tolerance ||
                abs(from.z - to.z) > tolerance
    }

    private inner class Warmup(
        val teleport: Teleport,
        delaySeconds: Int,
        private val applyCooldown: Boolean
    ) : BukkitRunnable() {
        private val origin = teleport.player.location.clone()
        private val startHealth = teleport.player.health
        private val deadline = System.currentTimeMillis() + delaySeconds * 1000L

        override fun run() {
            val player = teleport.player
            val reason = when {
                !player.isOnline -> CancelReason.LOGGED_OFF
                hasMoved(origin, player.location) -> CancelReason.MOVED
                player.health < startHealth -> CancelReason.DAMAGED
                else -> null
            }

            if (reason != null) {
                abort(player, reason)
                return
            }

            if (System.currentTimeMillis() >= deadline) {
                cancel()
                warmups.remove(player.uniqueId)
                complete(teleport, origin, applyCooldown)
            }
        }
    }
}