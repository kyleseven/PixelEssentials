package me.kyleseven.pixelessentials.managers

import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.utils.mmd
import me.kyleseven.pixelessentials.utils.mms
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

enum class TeleportDirection {
    REQUESTER_TO_TARGET,
    TARGET_TO_REQUESTER
}

sealed interface CreateInvitationResult {
    data object Created : CreateInvitationResult
    data object RecipientBusy : CreateInvitationResult
}

sealed interface AcceptInvitationResult {
    data object Accepted : AcceptInvitationResult
    data object NoPendingInvitation : AcceptInvitationResult
    data object RequesterOffline : AcceptInvitationResult
    data class MoverOnCooldown(val mover: Player, val remainingSeconds: Long) : AcceptInvitationResult
    data class CouldNotStart(val mover: Player) : AcceptInvitationResult
}

sealed interface DenyInvitationResult {
    data object Denied : DenyInvitationResult
    data object NoPendingInvitation : DenyInvitationResult
}

sealed interface CancelInvitationsResult {
    data class Canceled(val count: Int) : CancelInvitationsResult
    data object NoOutgoingInvitations : CancelInvitationsResult
}

class TeleportInvitationService(
    private val plugin: PixelEssentials,
    private val teleports: TeleportService
) {
    private val pendingByRecipient = mutableMapOf<UUID, TeleportInvitation>()

    fun create(
        requester: Player,
        target: Player,
        direction: TeleportDirection
    ): CreateInvitationResult {
        if (pendingByRecipient.containsKey(target.uniqueId)) {
            return CreateInvitationResult.RecipientBusy
        }

        val invitation = TeleportInvitation(
            requesterId = requester.uniqueId,
            requesterName = mms(requester.displayName()),
            targetId = target.uniqueId,
            targetName = mms(target.displayName()),
            direction = direction
        )
        pendingByRecipient[target.uniqueId] = invitation

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (pendingByRecipient.remove(invitation.targetId, invitation)) {
                Bukkit.getPlayer(invitation.requesterId)?.sendMessage(
                    mmd(
                        "<gray>Your teleport request to <white>${invitation.targetName}</white> " +
                                "has expired.</gray>"
                    )
                )
                Bukkit.getPlayer(invitation.targetId)?.sendMessage(
                    mmd(
                        "<gray>Teleport request from <white>${invitation.requesterName}</white> " +
                                "has expired.</gray>"
                    )
                )
            }
        }, plugin.configProvider.teleportRequestExpiration * 20L)

        return CreateInvitationResult.Created
    }

    fun accept(target: Player): AcceptInvitationResult {
        val invitation = pendingByRecipient[target.uniqueId]
            ?: return AcceptInvitationResult.NoPendingInvitation
        val requester = Bukkit.getPlayer(invitation.requesterId)
        if (requester == null) {
            pendingByRecipient.remove(target.uniqueId, invitation)
            return AcceptInvitationResult.RequesterOffline
        }

        val (mover, destinationPlayer) = when (invitation.direction) {
            TeleportDirection.REQUESTER_TO_TARGET -> requester to target
            TeleportDirection.TARGET_TO_REQUESTER -> target to requester
        }

        val result = teleports.schedule(
            TeleportPlan(
                player = mover,
                destination = { destinationPlayer.takeIf(Player::isOnline)?.location },
                destinationName = mms(destinationPlayer.displayName()),
                observer = destinationPlayer
            )
        )

        if (result is ScheduleTeleportResult.OnCooldown) {
            return AcceptInvitationResult.MoverOnCooldown(mover, result.remainingSeconds)
        }
        pendingByRecipient.remove(target.uniqueId, invitation)
        if (result is ScheduleTeleportResult.PlayerOffline ||
            result is ScheduleTeleportResult.DestinationUnavailable ||
            result is ScheduleTeleportResult.TeleportFailed
        ) {
            return AcceptInvitationResult.CouldNotStart(mover)
        }

        requester.sendMessage(
            mmd("<white>${mms(target.displayName())}</white> <green>accepted</green> <gray>your teleport request.</gray>")
        )
        target.sendMessage(
            mmd(
                "<green>Accepted</green> <gray>teleport request from</gray> " +
                        "<white>${mms(requester.displayName())}</white><gray>.</gray>"
            )
        )
        return AcceptInvitationResult.Accepted
    }

    fun deny(target: Player): DenyInvitationResult {
        val invitation = pendingByRecipient.remove(target.uniqueId)
            ?: return DenyInvitationResult.NoPendingInvitation
        val requester = Bukkit.getPlayer(invitation.requesterId)

        requester?.sendMessage(
            mmd("<white>${mms(target.displayName())}</white> <red>denied</red> <gray>your teleport request.</gray>")
        )
        target.sendMessage(
            mmd(
                "<red>Denied</red> <gray>teleport request from</gray> " +
                        "<white>${requester?.let { mms(it.displayName()) } ?: "Unknown Player"}</white><gray>.</gray>"
            )
        )
        return DenyInvitationResult.Denied
    }

    fun cancelOutgoing(requester: Player): CancelInvitationsResult {
        val invitations = pendingByRecipient.values.filter { it.requesterId == requester.uniqueId }
        if (invitations.isEmpty()) return CancelInvitationsResult.NoOutgoingInvitations

        invitations.forEach { invitation ->
            pendingByRecipient.remove(invitation.targetId, invitation)
            Bukkit.getPlayer(invitation.targetId)?.sendMessage(
                mmd(
                    "<gray>Teleport request from <white>${mms(requester.displayName())}</white> " +
                            "has been canceled.</gray>"
                )
            )
        }
        return CancelInvitationsResult.Canceled(invitations.size)
    }

    private data class TeleportInvitation(
        val requesterId: UUID,
        val requesterName: String,
        val targetId: UUID,
        val targetName: String,
        val direction: TeleportDirection
    )
}
