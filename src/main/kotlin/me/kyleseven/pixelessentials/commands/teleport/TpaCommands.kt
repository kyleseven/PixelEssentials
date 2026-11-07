package me.kyleseven.pixelessentials.commands.teleport

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.managers.teleport.IncomingRequest
import me.kyleseven.pixelessentials.managers.teleport.Teleport
import me.kyleseven.pixelessentials.managers.teleport.TpaType
import me.kyleseven.pixelessentials.utils.displayNameOf
import me.kyleseven.pixelessentials.utils.mmd
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class TpaCommands(private val plugin: PixelEssentials) : BaseCommand() {
    @CommandAlias("tpa")
    @Description("Request to teleport to a player")
    @CommandPermission("pixelessentials.tpa")
    @CommandCompletion("@players")
    fun onTpa(player: Player, target: OnlinePlayer) {
        sendRequest(player, target.player, TpaType.TPA)
    }

    @CommandAlias("tpahere")
    @Description("Request a player to teleport to you")
    @CommandPermission("pixelessentials.tpahere")
    @CommandCompletion("@players")
    fun onTpahere(player: Player, target: OnlinePlayer) {
        sendRequest(player, target.player, TpaType.TPAHERE)
    }

    @CommandAlias("tpaall")
    @Description("Request all players to teleport to you")
    @CommandPermission("pixelessentials.tpaall")
    fun onTpaall(player: Player) {
        val targets = Bukkit.getOnlinePlayers().filter {
            it != player && plugin.tpaManager.send(player, it, TpaType.TPAHERE)
        }
        targets.forEach { sendRequestReceivedMessage(player, it, TpaType.TPAHERE) }

        when (targets.size) {
            0 -> player.sendMessage(mmd("<red>No players to send requests to.</red>"))
            1 -> player.sendMessage(mmd("<gray>Sent request to 1 player.</gray>"))
            else -> player.sendMessage(mmd("<gray>Sent request to ${targets.size} players.</gray>"))
        }
    }

    @CommandAlias("tpall")
    @Description("Teleport all players to you")
    @CommandPermission("pixelessentials.tpall")
    fun onTpall(player: Player) {
        Bukkit.getOnlinePlayers().filter { it != player }.forEach {
            plugin.teleportManager.start(
                Teleport(
                    player = it,
                    destinationName = player.displayName(),
                    destination = { player.takeIf { p -> p.isOnline }?.location }
                ),
                delaySeconds = 0,
                applyCooldown = false
            )
        }

        player.sendMessage(mmd("<gray>All players have been teleported to you.</gray>"))
    }

    @CommandAlias("tpaccept")
    @Description("Accept a teleport request")
    @CommandPermission("pixelessentials.tpaccept")
    fun onTpaccept(player: Player) {
        when (val request = plugin.tpaManager.takeIncoming(player)) {
            IncomingRequest.None -> player.sendMessage(mmd("<gray>You have no pending teleport requests.</gray>"))
            IncomingRequest.RequesterOffline -> player.sendMessage(mmd("<gray>The requester is no longer online.</gray>"))
            is IncomingRequest.Found -> {
                request.requester.sendMessage(
                    mmd(
                        "<white><player></white> <green>accepted</green> <gray>your teleport request.</gray>",
                        Placeholder.component("player", player.displayName())
                    )
                )
                player.sendMessage(
                    mmd(
                        "<green>Accepted</green> <gray>teleport request from</gray> <white><player></white><gray>.</gray>",
                        Placeholder.component("player", request.requester.displayName())
                    )
                )
                plugin.teleportManager.start(request.toTeleport())
            }
        }
    }

    @CommandAlias("tpdeny")
    @Description("Deny a teleport request")
    @CommandPermission("pixelessentials.tpdeny")
    fun onTpdeny(player: Player) {
        when (val request = plugin.tpaManager.takeIncoming(player)) {
            IncomingRequest.None -> player.sendMessage(mmd("<gray>You have no pending teleport requests.</gray>"))
            IncomingRequest.RequesterOffline -> player.sendMessage(mmd("<gray>The requester is no longer online.</gray>"))
            is IncomingRequest.Found -> {
                request.requester.sendMessage(
                    mmd(
                        "<white><player></white> <red>denied</red> <gray>your teleport request.</gray>",
                        Placeholder.component("player", player.displayName())
                    )
                )
                player.sendMessage(
                    mmd(
                        "<red>Denied</red> <gray>teleport request from</gray> <white><player></white><gray>.</gray>",
                        Placeholder.component("player", request.requester.displayName())
                    )
                )
            }
        }
    }

    @CommandAlias("tpacancel")
    @Description("Cancel your outgoing teleport requests")
    @CommandPermission("pixelessentials.tpacancel")
    fun onTpacancel(player: Player) {
        val canceled = plugin.tpaManager.cancelOutgoing(player)
        canceled.forEach {
            Bukkit.getPlayer(it.target)?.sendMessage(
                mmd(
                    "<gray>Teleport request from <white><player></white> has been canceled.</gray>",
                    Placeholder.component("player", player.displayName())
                )
            )
        }

        when (canceled.size) {
            0 -> player.sendMessage(mmd("<red>You don't have any outgoing teleport requests.</red>"))
            1 -> player.sendMessage(
                mmd(
                    "<gray>Your teleport request to <white><player></white> has been canceled.</gray>",
                    Placeholder.component("player", displayNameOf(canceled.single().target))
                )
            )

            else -> player.sendMessage(mmd("<gray>Canceled <white>${canceled.size}</white> teleport requests.</gray>"))
        }
    }

    private fun sendRequest(requester: Player, target: Player, type: TpaType) {
        if (requester.uniqueId == target.uniqueId) {
            requester.sendMessage(mmd("<red>You can't teleport to yourself.</red>"))
            return
        }

        // Check the cooldown of whoever would be moved
        val mover = when (type) {
            TpaType.TPA -> requester
            TpaType.TPAHERE -> target
        }
        if (rejectIfOnCooldown(plugin, mover, notify = requester)) return

        if (!plugin.tpaManager.send(requester, target, type)) {
            requester.sendMessage(
                mmd(
                    "<white><player></white> <gray>already has a pending teleport request.</gray>",
                    Placeholder.component("player", target.displayName())
                )
            )
            return
        }

        requester.sendMessage(
            mmd(
                "<gray>Sent teleport request to</gray> <white><player></white><gray>.</gray>",
                Placeholder.component("player", target.displayName())
            )
        )
        sendRequestReceivedMessage(requester, target, type)
    }

    private fun sendRequestReceivedMessage(requester: Player, target: Player, type: TpaType) {
        val action = when (type) {
            TpaType.TPA -> "wants to teleport to you"
            TpaType.TPAHERE -> "wants you to teleport to them"
        }
        target.sendMessage(
            mmd(
                "<white><player></white> <gray>$action.</gray>\n" +
                        "<gray>Use</gray> <hover:show_text:'<green>Click to accept request.</green>'>" +
                        "<click:run_command:'/tpaccept'><green>/tpaccept</green></click></hover> " +
                        "<gray>to accept this request.</gray>\n" +
                        "<gray>Use</gray> <hover:show_text:'<red>Click to deny request.</red>'>" +
                        "<click:run_command:'/tpdeny'><red>/tpdeny</red></click></hover> " +
                        "<gray>to deny this request.</gray>",
                Placeholder.component("player", requester.displayName())
            )
        )
    }
}