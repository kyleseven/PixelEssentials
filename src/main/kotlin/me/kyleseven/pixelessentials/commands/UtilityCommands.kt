package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.utils.formatDate
import me.kyleseven.pixelessentials.utils.formatDuration
import me.kyleseven.pixelessentials.utils.mmd
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UtilityCommands(private val plugin: PixelEssentials) : BaseCommand() {
    @CommandAlias("motd")
    @Description("Get the message of the day")
    @CommandPermission("pixelessentials.motd")
    fun onMotd(sender: CommandSender) {
        val motd = plugin.configProvider.motd
        sender.sendMessage(mmd(motd))
    }

    @CommandAlias("list")
    @Description("List all online players")
    @CommandPermission("pixelessentials.list")
    fun onList(sender: CommandSender) {
        val players = plugin.server.onlinePlayers
        val playerList =
            "<gray>Online Players (${players.size}): </gray>" + players.joinToString(separator = "<gray>, </gray>") { "<white>$it.name</white>" }

        sender.sendMessage(mmd(playerList))
    }

    @CommandAlias("ping")
    @Description("Check your ping")
    @CommandPermission("pixelessentials.ping")
    fun onPing(sender: CommandSender, target: Player?) {
        if (target == null) {
            val ping = plugin.server.getPlayer(sender.name)?.ping ?: -1
            sender.sendMessage(mmd("<gray>Your ping: <white>$ping</white>ms</gray>"))
            return
        } else {
            val ping = target.ping
            sender.sendMessage(mmd("<gray>${target.name}'s ping: <white>$ping</white>ms</gray>"))
        }
    }

    @CommandAlias("whois")
    @Description("Get information about a player")
    @CommandPermission("pixelessentials.whois")
    fun onWhois(sender: CommandSender, target: Player) {
        val player = plugin.playerRepository.getPlayer(target.uniqueId)
        if (player == null) {
            sender.sendMessage(mmd("<red>Player not found in database</red>"))
            return
        }

        val firstJoin = formatDate("yyyy/MM/dd HH:mm:ss", player.firstJoin * 1000L)
        val lastSeen = formatDate("yyyy/MM/dd HH:mm:ss", player.lastSeen * 1000L)
        val totalPlaytime = formatDuration(player.totalPlaytime * 1000L)

        sender.sendMessage(
            mmd(
                """
                <gray>Player Information</gray>
                <gray>Username: <white>${player.lastAccountName}</white></gray>
                <gray>UUID: <white>${player.uuid}</white></gray>
                <gray>First Join: <white>$firstJoin</white></gray>
                <gray>Last Seen: <white>$lastSeen</white></gray>
                <gray>Total Playtime: <white>$totalPlaytime</white></gray>
                <gray>IP Address: <white>${player.ipAddress}</white></gray>
                <gray>Banned: <white>${if (player.isBanned) "Yes" else "No"}</white></gray>
                <gray>Ban Reason: <white>${player.banReason ?: "N/A"}</white></gray>
                """.trimIndent()
            )
        )
    }

    @CommandAlias("seen")
    @Description("Check when a player was last seen")
    @CommandPermission("pixelessentials.seen")
    fun onSeen(sender: CommandSender, target: Player) {
        val player = plugin.playerRepository.getPlayer(target.uniqueId)
        if (player == null) {
            sender.sendMessage(mmd("<red>Player not found in database</red>"))
            return
        }

        val lastSeen = formatDate("yyyy/MM/dd HH:mm:ss", player.lastSeen * 1000L)
        sender.sendMessage(mmd("<gray>${target.name} was last seen on <white>$lastSeen</white></gray>"))
    }
}