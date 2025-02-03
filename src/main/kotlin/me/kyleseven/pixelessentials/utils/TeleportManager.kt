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

    fun addRequest(requester: Player, target: Player, isToRequester: Boolean): Boolean {
        if (pendingInvitations.containsKey(target.uniqueId)) {
            requester.sendMessage(mmd("<white>${mms(target.displayName())}</white> <gray>already has a pending teleport request.</gray>"))
            return false
        }

        val request = TeleportInvitation(requester.uniqueId, target.uniqueId, isToRequester, System.currentTimeMillis())
        pendingInvitations[target.uniqueId] = request

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

        if (request.isToRequester) {
            processTpahere(requester, target)
        } else {
            processTpa(requester, target)
        }

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

    private fun processTpa(requester: Player, target: Player) {
        handleTeleport(
            playerToMove = requester,
            getDestination = { target.location },
            delay = plugin.configProvider.teleportDelay,
            requester = requester,
            target = target,
            message = "<gray>Teleporting to</gray> <white>${mms(target.displayName())}</white> <gray>in %d seconds. Do not move.</gray>"
        )
    }

    private fun processTpahere(requester: Player, target: Player) {
        handleTeleport(
            playerToMove = target,
            getDestination = { requester.location },
            delay = plugin.configProvider.teleportDelay,
            requester = requester,
            target = target,
            message = "<gray>Teleporting to</gray> <white>${mms(requester.displayName())}</white> <gray>in %d seconds. Do not move.</gray>"
        )
    }

    fun processTpall(requester: Player) {
        Bukkit.getOnlinePlayers().forEach {
            if (it != requester) it.teleport(requester.location)
        }
        requester.sendMessage(mmd("<gray>All players have been teleported to you.</gray>"))
    }

    private fun handleTeleport(
        playerToMove: Player,
        getDestination: () -> Location,
        delay: Int,
        requester: Player,
        target: Player,
        message: String
    ) {
        if (delay <= 0) {
            playerToMove.teleport(getDestination())
            setCooldown(requester)
            return
        }

        val initialLocation = playerToMove.location.clone()
        val taskId = object : BukkitRunnable() {
            override fun run() {
                activeTeleports.remove(playerToMove.uniqueId)
                if (playerToMove.isOnline) {
                    playerToMove.teleport(getDestination())
                    setCooldown(requester)
                }
            }
        }.runTaskLater(plugin, delay * 20L).taskId

        activeTeleports[playerToMove.uniqueId] = ActiveTeleport(
            taskId = taskId,
            initialLocation = initialLocation,
            requester = requester.uniqueId,
            target = target.uniqueId
        )

        playerToMove.sendMessage(mmd(message.format(delay)))
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val request = activeTeleports[player.uniqueId] ?: return

        if (hasMoved(event.from, event.to)) {
            Bukkit.getScheduler().cancelTask(request.taskId)
            activeTeleports.remove(player.uniqueId)

            player.sendMessage(mmd("<red>Teleport was canceled because you moved.</red>"))
            Bukkit.getPlayer(request.target)
                ?.sendMessage(mmd("<red>Teleport from ${mms(player.displayName())} was canceled because they moved!</red>"))
        }
    }

    private fun hasMoved(from: Location, to: Location): Boolean {
        val tolerance = 0.5
        return abs(from.x - to.x) > tolerance || abs(from.y - to.y) > tolerance || abs(from.z - to.z) > tolerance
    }
}