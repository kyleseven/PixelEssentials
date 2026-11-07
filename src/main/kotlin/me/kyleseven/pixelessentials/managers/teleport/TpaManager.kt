package me.kyleseven.pixelessentials.managers.teleport

import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.utils.displayNameOf
import me.kyleseven.pixelessentials.utils.mmd
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * Tracks /tpa and /tpahere requests. Each player can have at most one incoming request at a time.
 */
class TpaManager(private val plugin: PixelEssentials) {
    // Keyed by target
    private val pendingRequests = mutableMapOf<UUID, TpaRequest>()

    /**
     * Send a request that expires after the configured time.
     *
     * @return False if the target already has a pending request.
     */
    fun send(requester: Player, target: Player, type: TpaType): Boolean {
        if (target.uniqueId in pendingRequests) return false

        val request = TpaRequest(requester.uniqueId, target.uniqueId, type)
        pendingRequests[target.uniqueId] = request

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            expire(request)
        }, plugin.configProvider.teleportRequestExpiration * 20L)

        return true
    }

    /**
     * Remove and return a player's incoming request so they can accept or deny it.
     */
    fun takeIncoming(target: Player): IncomingRequest {
        val request = pendingRequests.remove(target.uniqueId) ?: return IncomingRequest.None
        val requester = Bukkit.getPlayer(request.requester) ?: return IncomingRequest.RequesterOffline
        return IncomingRequest.Found(requester, target, request.type)
    }

    /**
     * Cancel all of a player's outgoing requests.
     *
     * @return The canceled requests.
     */
    fun cancelOutgoing(requester: Player): List<TpaRequest> {
        val outgoing = pendingRequests.values.filter { it.requester == requester.uniqueId }
        outgoing.forEach { pendingRequests.remove(it.target) }
        return outgoing
    }

    private fun expire(request: TpaRequest) {
        // Identity check, since the target may have resolved this request and received an identical one since
        if (pendingRequests[request.target] !== request) return
        pendingRequests.remove(request.target)

        Bukkit.getPlayer(request.requester)?.sendMessage(
            mmd(
                "<gray>Your teleport request to <white><player></white> has expired.</gray>",
                Placeholder.component("player", displayNameOf(request.target))
            )
        )
        Bukkit.getPlayer(request.target)?.sendMessage(
            mmd(
                "<gray>Teleport request from <white><player></white> has expired.</gray>",
                Placeholder.component("player", displayNameOf(request.requester))
            )
        )
    }
}