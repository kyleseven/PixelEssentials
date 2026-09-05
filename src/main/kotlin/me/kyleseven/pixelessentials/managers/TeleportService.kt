package me.kyleseven.pixelessentials.managers

import me.kyleseven.pixelessentials.PixelEssentials
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import kotlin.math.abs

data class TeleportPlan(
    val player: Player,
    val destination: () -> Location?,
    val destinationName: String,
    val observer: Player? = null
)

data class TeleportPolicy(
    val delaySeconds: Int,
    val applyCooldown: Boolean = true,
    val recordBackLocation: Boolean = true,
    val cancelOnMovement: Boolean = true,
    val cancelOnDamage: Boolean = true
) {
    companion object {
        fun standard(delaySeconds: Int) = TeleportPolicy(delaySeconds)

        val ADMIN = TeleportPolicy(
            delaySeconds = 0,
            applyCooldown = false
        )
    }
}

sealed interface ScheduleTeleportResult {
    data object Started : ScheduleTeleportResult
    data object Completed : ScheduleTeleportResult
    data class OnCooldown(val remainingSeconds: Long) : ScheduleTeleportResult
    data object PlayerOffline : ScheduleTeleportResult
    data object DestinationUnavailable : ScheduleTeleportResult
    data object TeleportFailed : ScheduleTeleportResult
}

class TeleportService(private val plugin: PixelEssentials) {
    private val cooldowns = mutableMapOf<UUID, Long>()
    private val activeTeleports = mutableMapOf<UUID, BukkitTask>()
    private val backLocations = mutableMapOf<UUID, Location>()

    fun recordBackLocation(player: Player, location: Location) {
        backLocations[player.uniqueId] = location.clone()
    }

    fun getBackLocation(player: Player): Location? = backLocations[player.uniqueId]?.clone()

    fun getRemainingCooldown(player: Player): Long {
        val expiresAt = cooldowns[player.uniqueId] ?: return 0
        val remainingMillis = (expiresAt - System.currentTimeMillis()).coerceAtLeast(0)
        if (remainingMillis == 0L) {
            cooldowns.remove(player.uniqueId)
            return 0
        }
        return (remainingMillis + 999) / 1000
    }

    fun schedule(
        plan: TeleportPlan,
        policy: TeleportPolicy = TeleportPolicy.standard(plugin.configProvider.teleportDelay)
    ): ScheduleTeleportResult {
        if (!plan.player.isOnline) return ScheduleTeleportResult.PlayerOffline

        if (policy.applyCooldown) {
            val remainingCooldown = getRemainingCooldown(plan.player)
            if (remainingCooldown > 0) return ScheduleTeleportResult.OnCooldown(remainingCooldown)
        }

        cancelActiveTeleport(plan.player)

        val effectiveDelay = if (
            plan.player.hasPermission(DELAY_BYPASS_PERMISSION) || policy.delaySeconds <= 0
        ) 0 else policy.delaySeconds

        if (effectiveDelay == 0) return execute(plan, policy, plan.player.location.clone())

        val initialLocation = plan.player.location.clone()
        val initialHealth = plan.player.health
        val startTime = System.currentTimeMillis()

        val runnable = object : BukkitRunnable() {
            override fun run() {
                val cancellationReason = when {
                    !plan.player.isOnline -> TeleportCancellation.LOGGED_OFF
                    policy.cancelOnMovement && hasMoved(initialLocation, plan.player.location) ->
                        TeleportCancellation.MOVED
                    policy.cancelOnDamage && plan.player.health < initialHealth ->
                        TeleportCancellation.DAMAGED
                    else -> null
                }

                if (cancellationReason != null) {
                    cancel()
                    activeTeleports.remove(plan.player.uniqueId)
                    notifyCancellation(plan, cancellationReason)
                    return
                }

                if (System.currentTimeMillis() >= startTime + effectiveDelay * 1000L) {
                    cancel()
                    activeTeleports.remove(plan.player.uniqueId)
                    execute(plan, policy, initialLocation)
                }
            }
        }

        activeTeleports[plan.player.uniqueId] = runnable.runTaskTimer(plugin, 1L, 5L)
        plan.player.sendMessage(TeleportMessages.warmup(plan.destinationName, effectiveDelay))
        return ScheduleTeleportResult.Started
    }

    private fun execute(
        plan: TeleportPlan,
        policy: TeleportPolicy,
        previousLocation: Location
    ): ScheduleTeleportResult {
        val destination = plan.destination()
        if (destination == null) {
            plan.player.sendMessage(TeleportMessages.destinationUnavailable())
            return ScheduleTeleportResult.DestinationUnavailable
        }

        if (!plan.player.teleport(destination)) {
            plan.player.sendMessage(TeleportMessages.teleportFailed())
            return ScheduleTeleportResult.TeleportFailed
        }

        if (policy.recordBackLocation && plan.player.hasPermission(BACK_ON_TELEPORT_PERMISSION)) {
            recordBackLocation(plan.player, previousLocation)
        }
        if (policy.applyCooldown) setCooldown(plan.player)

        plan.player.sendMessage(TeleportMessages.teleported(plan.destinationName))
        return ScheduleTeleportResult.Completed
    }

    private fun setCooldown(player: Player) {
        if (player.hasPermission(COOLDOWN_BYPASS_PERMISSION)) return
        cooldowns[player.uniqueId] =
            System.currentTimeMillis() + plugin.configProvider.teleportCooldown * 1000L
    }

    private fun cancelActiveTeleport(player: Player) {
        activeTeleports.remove(player.uniqueId)?.let {
            it.cancel()
            player.sendMessage(TeleportMessages.replaced())
        }
    }

    private fun notifyCancellation(plan: TeleportPlan, reason: TeleportCancellation) {
        if (plan.player.isOnline) {
            plan.player.sendMessage(TeleportMessages.canceledForPlayer(reason))
        }
        plan.observer
            ?.takeIf(Player::isOnline)
            ?.sendMessage(TeleportMessages.canceledForObserver(plan.player, reason))
    }

    private fun hasMoved(from: Location, to: Location): Boolean {
        if (from.world != to.world) return true
        return abs(from.x - to.x) > MOVEMENT_TOLERANCE ||
                abs(from.y - to.y) > MOVEMENT_TOLERANCE ||
                abs(from.z - to.z) > MOVEMENT_TOLERANCE
    }

    private companion object {
        const val MOVEMENT_TOLERANCE = 0.5
        const val DELAY_BYPASS_PERMISSION = "pixelessentials.teleport.delay.bypass"
        const val COOLDOWN_BYPASS_PERMISSION = "pixelessentials.teleport.cooldown.bypass"
        const val BACK_ON_TELEPORT_PERMISSION = "pixelessentials.back.onteleport"
    }
}

enum class TeleportCancellation {
    LOGGED_OFF,
    MOVED,
    DAMAGED
}
