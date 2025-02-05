package me.kyleseven.pixelessentials.utils

import me.kyleseven.pixelessentials.PixelEssentials
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.math.abs

sealed class TeleportRequest {
    data class PlayerToPlayer(
        val requester: Player,
        val target: Player,
        val isToRequester: Boolean
    ) : TeleportRequest()

    data class ToLocation(
        val player: Player,
        val locationProvider: () -> Location,
        val destinationName: String
    ) : TeleportRequest()
}

class TeleportManager(private val plugin: PixelEssentials) : Listener {
    private val cooldowns = mutableMapOf<UUID, Long>()
    private val activeTeleports = mutableMapOf<UUID, ActiveTeleport>()
    private val pendingInvitations = mutableMapOf<UUID, TeleportInvitation>()

    private data class ActiveTeleport(
        val taskId: Int,
        val initialLocation: Location,
        val requester: UUID,
        val target: UUID
    )

    private data class TeleportInvitation(
        val requester: UUID,
        val target: UUID,
        val isToRequester: Boolean,
        val timestamp: Long
    )

    fun isOnCooldown(player: Player): Boolean {
        val remaining = getRemainingCooldown(player)
        return remaining > 0
    }

    fun getRemainingCooldown(player: Player): Long {
        return ((cooldowns[player.uniqueId] ?: 0) - System.currentTimeMillis()).coerceAtLeast(0) / 1000
    }

    private fun setCooldown(player: Player) {
        cooldowns[player.uniqueId] = System.currentTimeMillis() + (plugin.configProvider.teleportCooldown * 1000L)
    }

    fun scheduleTeleport(
        request: TeleportRequest,
        delaySeconds: Int = plugin.configProvider.teleportDelay,
        applyCooldown: Boolean = true
    ) {
        val (playerToMove, destination, messageTemplate) = when (request) {
            is TeleportRequest.PlayerToPlayer -> Triple(
                if (request.isToRequester) request.target else request.requester,
                { request.target.location },
                "<gray>Teleporting to</gray> <white>${mms(request.target.displayName())}</white> <gray>in %d seconds. Do not move.</gray>"
            )

            is TeleportRequest.ToLocation -> Triple(
                request.player,
                request.locationProvider,
                "<gray>Teleporting to</gray> <white>${request.destinationName}</white> <gray>in %d seconds. Do not move.</gray>"
            )
        }

        // Cancel any existing teleport
        cancelExistingTeleport(playerToMove)

        // Immediate teleport if no delay
        if (delaySeconds <= 0) {
            playerToMove.teleport(destination())
            if (applyCooldown) {
                setCooldown(playerToMove)
            }
            return
        }

        // Schedule delayed teleport
        val initialLocation = playerToMove.location.clone()
        val taskId = object : BukkitRunnable() {
            override fun run() {
                activeTeleports.remove(playerToMove.uniqueId)
                if (playerToMove.isOnline) {
                    val targetLocation = destination()
                    playerToMove.teleport(targetLocation)
                    if (applyCooldown) {
                        setCooldown(playerToMove)
                    }
                    playerToMove.sendMessage(
                        mmd(
                            "<gray>Teleported to</gray> <white>${
                                when (request) {
                                    is TeleportRequest.PlayerToPlayer -> mms(request.target.displayName())
                                    is TeleportRequest.ToLocation -> request.destinationName
                                }
                            }</white><gray>.</gray>"
                        )
                    )
                }
            }
        }.runTaskLater(plugin, delaySeconds * 20L).taskId

        // Store active teleport
        activeTeleports[playerToMove.uniqueId] = ActiveTeleport(
            taskId = taskId,
            initialLocation = initialLocation,
            requester = when (request) {
                is TeleportRequest.PlayerToPlayer -> request.requester.uniqueId
                is TeleportRequest.ToLocation -> playerToMove.uniqueId
            },
            target = when (request) {
                is TeleportRequest.PlayerToPlayer -> request.target.uniqueId
                is TeleportRequest.ToLocation -> playerToMove.uniqueId
            }
        )

        playerToMove.sendMessage(mmd(messageTemplate.format(delaySeconds)))
        return
    }

    fun addRequest(requester: Player, target: Player, isToRequester: Boolean): Boolean {
        if (pendingInvitations.containsKey(target.uniqueId)) {
            requester.sendMessage(mmd("<white>${mms(target.displayName())}</white> <gray>already has a pending teleport request.</gray>"))
            return false
        }

        val request = TeleportInvitation(requester.uniqueId, target.uniqueId, isToRequester, System.currentTimeMillis())
        pendingInvitations[target.uniqueId] = request

        // Schedule request expiration
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (pendingInvitations[target.uniqueId] == request) {
                pendingInvitations.remove(target.uniqueId)
                requester.sendMessage(mmd("<gray>Your teleport request to <white>${mms(target.displayName())}</white> has expired.</gray>"))
                target.sendMessage(mmd("<gray>Teleport request from <white>${mms(requester.displayName())}</white> has expired.</gray>"))
            }
        }, plugin.configProvider.teleportRequestExpiration * 20L)

        return true
    }

    fun acceptRequest(target: Player): Boolean {
        val request = pendingInvitations.remove(target.uniqueId) ?: return false
        val requester = Bukkit.getPlayer(request.requester) ?: run {
            target.sendMessage(mmd("<gray>The requester is no longer online.</gray>"))
            return false
        }

        scheduleTeleport(
            TeleportRequest.PlayerToPlayer(
                requester = requester,
                target = target,
                isToRequester = request.isToRequester
            )
        )

        requester.sendMessage(mmd("<white>${mms(target.displayName())}</white> <green>accepted</green> <gray>your teleport request.</gray>"))
        target.sendMessage(mmd("<green>Accepted</green> <gray>teleport request from</gray> <white>${mms(requester.displayName())}</white><gray>.</gray>"))
        return true
    }

    fun denyRequest(target: Player): Boolean {
        val request = pendingInvitations.remove(target.uniqueId) ?: return false
        val requester = Bukkit.getPlayer(request.requester)

        requester?.sendMessage(mmd("<white>${mms(target.displayName())}</white> <red>denied</red> <gray>your teleport request.</gray>"))
        target.sendMessage(mmd("<red>Denied</red> <gray>teleport request from</gray> <white>${requester?.displayName() ?: "Unknown Player"}</white><gray>.</gray>"))
        return true
    }

    fun cancelRequest(requester: Player) {
        val request = pendingInvitations.entries.find { it.value.requester == requester.uniqueId }
        if (request == null) {
            requester.sendMessage(mmd("<red>You don't have any outgoing teleport requests.</red>"))
            return
        }

        pendingInvitations.remove(request.key)
        val target = Bukkit.getPlayer(request.value.target)

        target?.sendMessage(mmd("<gray>Teleport request from <white>${mms(requester.displayName())}</white> has been canceled.</gray>"))
        requester.sendMessage(
            mmd(
                "<gray>Your teleport request to <white>${
                    target?.displayName()?.let { mms(it) } ?: "Unknown Player"
                }</white> has been canceled.</gray>"
            )
        )
    }

    private fun cancelExistingTeleport(player: Player) {
        val existingTeleport = activeTeleports[player.uniqueId]
        if (existingTeleport != null) {
            Bukkit.getScheduler().cancelTask(existingTeleport.taskId)
            activeTeleports.remove(player.uniqueId)
            player.sendMessage(mmd("<red>Previous teleport cancelled due to new teleport request.</red>"))
        }
    }

    fun processTpall(requester: Player) {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (player != requester) {
                scheduleTeleport(
                    TeleportRequest.ToLocation(
                        player = player,
                        locationProvider = { requester.location },
                        destinationName = mms(requester.displayName())
                    ),
                    delaySeconds = 0,
                    applyCooldown = false
                )
            }
        }
        requester.sendMessage(mmd("<gray>All players have been teleported to you.</gray>"))
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val activeTeleport = activeTeleports[player.uniqueId] ?: return

        if (hasMoved(event.from, event.to)) {
            Bukkit.getScheduler().cancelTask(activeTeleport.taskId)
            activeTeleports.remove(player.uniqueId)

            player.sendMessage(mmd("<red>Teleport was canceled because you moved.</red>"))
            if (activeTeleport.requester != activeTeleport.target) {
                Bukkit.getPlayer(activeTeleport.target)
                    ?.sendMessage(mmd("<red>Teleport from ${mms(player.displayName())} was canceled because they moved!</red>"))
            }
        }
    }

    private fun hasMoved(from: Location, to: Location): Boolean {
        val tolerance = 0.5
        return abs(from.x - to.x) > tolerance ||
                abs(from.y - to.y) > tolerance ||
                abs(from.z - to.z) > tolerance
    }
}