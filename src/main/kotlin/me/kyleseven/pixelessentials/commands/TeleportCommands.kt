package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.kyleseven.pixelessentials.PixelEssentials
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class TeleportCommands(private val plugin: PixelEssentials) : BaseCommand() {
    @CommandAlias("tpa")
    @Description("Request to teleport to a player")
    @CommandPermission("pixelessentials.tpa")
    @CommandCompletion("@players")
    fun onTpa(player: Player, target: OnlinePlayer) {
        if (plugin.teleportManager.isOnCooldown(player)) {
            player.sendMessage(
                MiniMessage.miniMessage()
                    .deserialize(
                        "<red>You are on cooldown for another</red> <white>${
                            plugin.teleportManager.getRemainingCooldown(player)
                        } seconds</white><red>.</red>"
                    )
            )
            return
        }

        if (player.uniqueId == target.player.uniqueId) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You can't teleport to yourself.</red>"))
            return
        }

        if (!plugin.teleportManager.addRequest(player, target.player, false)) {
            return
        }

        player.sendMessage(
            MiniMessage.miniMessage()
                .deserialize("<gray>Sent teleport request to</gray> <white>${target.player.displayName()}</white><gray>.</gray>")
        )
        target.player.sendMessage(
            MiniMessage.miniMessage().deserialize(
                "<white>${player.displayName()}</white> <gray>wants to teleport to you.</gray>\n" +
                        "<gray>Use</gray> <hover:show_text:'<green>Click to accept request.</green>'><click:run_command:'/tpaccept'><green>/tpaccept</green></click></hover> <gray>to accept this request.</gray>\n" + "<gray>Use</gray> <hover:show_text:'<red>Click to deny request.</red>'><click:run_command:'/tpdeny'><red>/tpdeny</red></click></hover> <gray>to deny this request.</gray>"
            )
        )

    }

    @CommandAlias("tpahere")
    @Description("Request a player to teleport to you")
    @CommandPermission("pixelessentials.tpahere")
    @CommandCompletion("@players")
    fun onTpahere(player: Player, target: OnlinePlayer) {
        if (plugin.teleportManager.isOnCooldown(player)) {
            player.sendMessage(
                MiniMessage.miniMessage()
                    .deserialize(
                        "<red>You are on cooldown for another</red> <white>${
                            plugin.teleportManager.getRemainingCooldown(player)
                        } seconds</white><red>.</red>"
                    )
            )
            return
        }

        if (player.uniqueId == target.player.uniqueId) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You can't teleport to yourself.</red>"))
            return
        }

        if (!plugin.teleportManager.addRequest(player, target.player, true)) {
            return
        }

        player.sendMessage(
            MiniMessage.miniMessage()
                .deserialize("<gray>Sent teleport request to</gray> <white>${target.player.displayName()}</white><gray>.</gray>")
        )
        target.player.sendMessage(
            MiniMessage.miniMessage().deserialize(
                "<white>${player.displayName()}</white> <gray>wants you to teleport to them.</gray>\n" +
                        "<gray>Use</gray> <hover:show_text:'<green>Click to accept request.</green>'><click:run_command:'/tpaccept'><green>/tpaccept</green></click></hover> <gray>to accept this request.</gray>\n" +
                        "<gray>Use</gray> <hover:show_text:'<red>Click to deny request.</red>'><click:run_command:'/tpdeny'><red>/tpdeny</red></click></hover> <gray>to deny this request.</gray>"
            )
        )

    }

    @CommandAlias("tpaall")
    @Description("Request all players to teleport to you")
    @CommandPermission("pixelessentials.tpaall")
    fun onTpaall(player: Player) {
        if (plugin.teleportManager.isOnCooldown(player)) {
            player.sendMessage(
                MiniMessage.miniMessage()
                    .deserialize(
                        "<red>You are on cooldown for another</red> <white>${
                            plugin.teleportManager.getRemainingCooldown(player)
                        } seconds</white><red>.</red>"
                    )
            )
            return
        }

        var successCount = 0
        Bukkit.getOnlinePlayers().forEach {
            if (it != player && plugin.teleportManager.addRequest(player, it, true)) {
                it.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                        "<white>${player.displayName()}</white> <gray>wants you to teleport to them.</gray>\n" +
                                "<gray>Use</gray> <hover:show_text:'<green>Click to accept request.</green>'><click:run_command:'/tpaccept'><green>/tpaccept</green></click></hover> <gray>to accept this request.</gray>\n" +
                                "<gray>Use</gray> <hover:show_text:'<red>Click to deny request.</red>'><click:run_command:'/tpdeny'><red>/tpdeny</red></click></hover> <gray>to deny this request.</gray>"
                    )
                )
                successCount++
            }
        }

        when (successCount) {
            0 -> player.sendMessage("§cNo players received your request")
            1 -> player.sendMessage("§aSent request to 1 player")
            else -> player.sendMessage("§aSent requests to $successCount players")
        }
    }

    @CommandAlias("tpall")
    @Description("Teleport all players to you")
    @CommandPermission("pixelessentials.tpall")
    fun onTpall(player: Player) {
        plugin.teleportManager.processTpall(player)
    }

    @CommandAlias("tpaccept")
    @Description("Accept a teleport request")
    fun onTpaccept(player: Player) {
        if (!plugin.teleportManager.acceptRequest(player)) {
            player.sendMessage(
                MiniMessage.miniMessage()
                    .deserialize("<red>You have no pending teleport requests.</red>")
            )
        }
    }

    @CommandAlias("tpdeny")
    @Description("Deny a teleport request")
    fun onTpdeny(player: Player) {
        if (!plugin.teleportManager.denyRequest(player)) {
            player.sendMessage(
                MiniMessage.miniMessage()
                    .deserialize("<red>You have no pending teleport requests.</red>")
            )
        }
    }
}