package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.kyleseven.pixelessentials.managers.AcceptInvitationResult
import me.kyleseven.pixelessentials.managers.CancelInvitationsResult
import me.kyleseven.pixelessentials.managers.CreateInvitationResult
import me.kyleseven.pixelessentials.managers.DenyInvitationResult
import me.kyleseven.pixelessentials.managers.TeleportDirection
import me.kyleseven.pixelessentials.managers.TeleportInvitationService
import me.kyleseven.pixelessentials.managers.TeleportMessages
import me.kyleseven.pixelessentials.managers.TeleportService
import me.kyleseven.pixelessentials.utils.mmd
import me.kyleseven.pixelessentials.utils.mms
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class TeleportRequestCommands(
    private val invitations: TeleportInvitationService,
    private val teleports: TeleportService
) : BaseCommand() {
    @CommandAlias("tpa")
    @Description("Request to teleport to a player")
    @CommandPermission("pixelessentials.tpa")
    @CommandCompletion("@players")
    fun onTpa(player: Player, target: OnlinePlayer) {
        createInvitation(player, target.player, TeleportDirection.REQUESTER_TO_TARGET)
    }

    @CommandAlias("tpahere")
    @Description("Request a player to teleport to you")
    @CommandPermission("pixelessentials.tpahere")
    @CommandCompletion("@players")
    fun onTpahere(player: Player, target: OnlinePlayer) {
        createInvitation(player, target.player, TeleportDirection.TARGET_TO_REQUESTER)
    }

    @CommandAlias("tpaall")
    @Description("Request all players to teleport to you")
    @CommandPermission("pixelessentials.tpaall")
    fun onTpaall(player: Player) {
        var successCount = 0
        Bukkit.getOnlinePlayers()
            .asSequence()
            .filter { it.uniqueId != player.uniqueId }
            .forEach { target ->
                if (invitations.create(player, target, TeleportDirection.TARGET_TO_REQUESTER) ==
                    CreateInvitationResult.Created
                ) {
                    sendInvitationMessage(player, target, TeleportDirection.TARGET_TO_REQUESTER)
                    successCount++
                }
            }

        val message = when (successCount) {
            0 -> "<red>No players to send requests to.</red>"
            1 -> "<gray>Sent request to 1 player.</gray>"
            else -> "<gray>Sent request to $successCount players.</gray>"
        }
        player.sendMessage(mmd(message))
    }

    @CommandAlias("tpaccept")
    @Description("Accept a teleport request")
    @CommandPermission("pixelessentials.tpaccept")
    fun onTpaccept(player: Player) {
        when (val result = invitations.accept(player)) {
            AcceptInvitationResult.Accepted -> Unit
            AcceptInvitationResult.NoPendingInvitation ->
                player.sendMessage(mmd("<gray>You have no pending teleport requests.</gray>"))
            AcceptInvitationResult.RequesterOffline ->
                player.sendMessage(mmd("<gray>The requester is no longer online.</gray>"))
            is AcceptInvitationResult.MoverOnCooldown -> {
                if (result.mover.uniqueId == player.uniqueId) {
                    player.sendMessage(TeleportMessages.cooldown(result.remainingSeconds))
                } else {
                    player.sendMessage(TeleportMessages.playerCooldown(result.mover, result.remainingSeconds))
                    result.mover.sendMessage(TeleportMessages.cooldown(result.remainingSeconds))
                }
            }
            is AcceptInvitationResult.CouldNotStart -> {
                if (result.mover.uniqueId != player.uniqueId) {
                    player.sendMessage(mmd("<red>The accepted teleport could not be started.</red>"))
                }
            }
        }
    }

    @CommandAlias("tpdeny")
    @Description("Deny a teleport request")
    @CommandPermission("pixelessentials.tpdeny")
    fun onTpdeny(player: Player) {
        if (invitations.deny(player) == DenyInvitationResult.NoPendingInvitation) {
            player.sendMessage(mmd("<gray>You have no pending teleport requests.</gray>"))
        }
    }

    @CommandAlias("tpacancel")
    @Description("Cancel outgoing teleport requests")
    @CommandPermission("pixelessentials.tpacancel")
    fun onTpacancel(player: Player) {
        when (val result = invitations.cancelOutgoing(player)) {
            CancelInvitationsResult.NoOutgoingInvitations ->
                player.sendMessage(mmd("<red>You don't have any outgoing teleport requests.</red>"))
            is CancelInvitationsResult.Canceled -> {
                val noun = if (result.count == 1) "request" else "requests"
                player.sendMessage(mmd("<gray>Canceled ${result.count} outgoing teleport $noun.</gray>"))
            }
        }
    }

    private fun createInvitation(
        requester: Player,
        target: Player,
        direction: TeleportDirection
    ) {
        if (requester.uniqueId == target.uniqueId) {
            requester.sendMessage(mmd("<red>You can't teleport to yourself.</red>"))
            return
        }

        val mover = if (direction == TeleportDirection.REQUESTER_TO_TARGET) requester else target
        val remainingCooldown = teleports.getRemainingCooldown(mover)
        if (remainingCooldown > 0) {
            if (mover.uniqueId == requester.uniqueId) {
                requester.sendMessage(TeleportMessages.cooldown(remainingCooldown))
            } else {
                requester.sendMessage(TeleportMessages.playerCooldown(mover, remainingCooldown))
            }
            return
        }

        when (invitations.create(requester, target, direction)) {
            CreateInvitationResult.Created -> {
                requester.sendMessage(
                    mmd("<gray>Sent teleport request to</gray> <white>${mms(target.displayName())}</white><gray>.</gray>")
                )
                sendInvitationMessage(requester, target, direction)
            }
            CreateInvitationResult.RecipientBusy -> requester.sendMessage(
                mmd("<white>${mms(target.displayName())}</white> <gray>already has a pending teleport request.</gray>")
            )
        }
    }

    private fun sendInvitationMessage(
        requester: Player,
        target: Player,
        direction: TeleportDirection
    ) {
        val requestType = when (direction) {
            TeleportDirection.REQUESTER_TO_TARGET -> "wants to teleport to you"
            TeleportDirection.TARGET_TO_REQUESTER -> "wants you to teleport to them"
        }
        target.sendMessage(
            mmd(
                "<white>${mms(requester.displayName())}</white> <gray>$requestType.</gray>\n" +
                        "<gray>Use</gray> <hover:show_text:'<green>Click to accept request.</green>'>" +
                        "<click:run_command:'/tpaccept'><green>/tpaccept</green></click></hover> " +
                        "<gray>to accept this request.</gray>\n" +
                        "<gray>Use</gray> <hover:show_text:'<red>Click to deny request.</red>'>" +
                        "<click:run_command:'/tpdeny'><red>/tpdeny</red></click></hover> " +
                        "<gray>to deny this request.</gray>"
            )
        )
    }
}
