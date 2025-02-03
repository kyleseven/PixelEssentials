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
        cooldowns[player.uniqueId] = System.currentTimeMillis() + (plugin.mainConfig.teleportCooldown * 1000L)
    }

    fun addRequest(requester: Player, target: Player, isToRequester: Boolean): Boolean {
        if (pendingInvitations.containsKey(target.uniqueId)) {
            requester.sendMessage("${target.name} already has a pending request.")
            return false
        }

        val request = TeleportInvitation(requester.uniqueId, target.uniqueId, isToRequester, System.currentTimeMillis())
        pendingInvitations[target.uniqueId] = request

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (pendingInvitations[target.uniqueId] == request) {
                pendingInvitations.remove(target.uniqueId)
                requester.sendMessage("§cYour teleport request to ${target.name} has expired.")
                target.sendMessage("§cTeleport request from ${requester.name} has expired.")
            }
        }, plugin.mainConfig.teleportRequestExpiration * 20L)

        return true
    }

    fun acceptRequest(target: Player): Boolean {
        val request = pendingInvitations.remove(target.uniqueId) ?: return false
        val requester = Bukkit.getPlayer(request.requester) ?: run {
            target.sendMessage("§cThe requester is no longer online.")
            return false
        }

        if (request.isToRequester) {
            processTpahere(requester, target)
        } else {
            processTpa(requester, target)
        }

        requester.sendMessage("§a${target.name} accepted your teleport request.")
        target.sendMessage("§aAccepted teleport request from ${requester.name}.")
        return true
    }

    fun denyRequest(target: Player): Boolean {
        val request = pendingInvitations.remove(target.uniqueId) ?: return false
        val requester = Bukkit.getPlayer(request.requester)
        requester?.sendMessage("§c${target.name} denied your teleport request.")
        target.sendMessage("§cDenied teleport request from ${requester?.name ?: "unknown player"}.")
        return true
    }

    private fun processTpa(requester: Player, target: Player) {
        handleTeleport(
            playerToMove = requester,
            getDestination = { target.location },
            delay = plugin.mainConfig.teleportDelay,
            requester = requester,
            target = target,
            message = "§eTeleporting to ${target.name} in %d seconds. Do not move!"
        )
    }

    private fun processTpahere(requester: Player, target: Player) {
        handleTeleport(
            playerToMove = target,
            getDestination = { requester.location },
            delay = plugin.mainConfig.teleportDelay,
            requester = requester,
            target = target,
            message = "§eTeleporting to ${requester.name} in %d seconds. Do not move!"
        )
    }

    fun processTpall(requester: Player) {
        Bukkit.getOnlinePlayers().forEach {
            if (it != requester) it.teleport(requester.location)
        }
        requester.sendMessage("§aAll players have been teleported to you.")
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

        playerToMove.sendMessage(message.format(delay))
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val request = activeTeleports[player.uniqueId] ?: return

        if (hasMoved(event.from, event.to)) {
            Bukkit.getScheduler().cancelTask(request.taskId)
            activeTeleports.remove(player.uniqueId)

            player.sendMessage("§cTeleport canceled due to movement!")
            Bukkit.getPlayer(request.requester)
                ?.sendMessage("§cTeleport to ${player.name} was canceled because they moved!")
        }
    }

    private fun hasMoved(from: Location, to: Location): Boolean {
        return from.blockX != to.blockX || from.blockY != to.blockY || from.blockZ != to.blockZ
    }
}