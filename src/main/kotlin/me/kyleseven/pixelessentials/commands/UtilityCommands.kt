package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.utils.formatDate
import me.kyleseven.pixelessentials.utils.formatDuration
import me.kyleseven.pixelessentials.utils.mmd
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class UtilityCommands(private val plugin: PixelEssentials) : BaseCommand() {
    @CommandAlias("motd")
    @Description("Get the message of the day")
    @CommandPermission("pixelessentials.motd")
    fun onMotd(sender: CommandSender) {
        sender.sendMessage(plugin.motdBuilder.build(sender))
    }

    @CommandAlias("list")
    @Description("List all online players")
    @CommandPermission("pixelessentials.list")
    fun onList(sender: CommandSender) {
        val players = plugin.server.onlinePlayers
        val playerList =
            "<gray>Online Players (${players.size}): </gray>" + players.joinToString(separator = "<gray>, </gray>") { "<white>${it.name}</white>" }

        sender.sendMessage(mmd(playerList))
    }

    @CommandAlias("ping")
    @Description("Check your ping")
    @CommandCompletion("@players")
    @CommandPermission("pixelessentials.ping")
    fun onPing(sender: CommandSender, @Optional playerName: String?) {
        val target = playerName?.let { Bukkit.getPlayer(it) }
        if (target == null) {
            sender.sendMessage(mmd("<red>Player not found</red>"))
            return
        }

        val ping = target.ping
        sender.sendMessage(mmd("<gray>${target.name}'s ping: <white>$ping</white>ms</gray>"))
    }

    @CommandAlias("whois")
    @Description("Get information about a player")
    @CommandCompletion("@players")
    @CommandPermission("pixelessentials.whois")
    fun onWhois(sender: CommandSender, playerName: String) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val player =
                playerName.let { Bukkit.getOfflinePlayer(it) }.let { plugin.playerRepository.getPlayer(it.uniqueId) }
            if (player == null) {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    sender.sendMessage(mmd("<red>Player not found in database</red>"))
                })
                return@Runnable
            }

            val firstJoin = formatDate("M/d/yyyy h:mm a", player.firstJoin * 1000L)
            val lastSeen = formatDate("M/d/yyyy h:mm a", player.lastSeen * 1000L)
            val totalPlaytime = formatDuration(player.totalPlaytime * 1000L)

            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
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
            })
        })
    }

    @CommandAlias("seen")
    @Description("Check when a player was last seen")
    @CommandCompletion("@players")
    @CommandPermission("pixelessentials.seen")
    fun onSeen(sender: CommandSender, playerName: String) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val offlinePlayer = playerName.let { Bukkit.getOfflinePlayer(it) }
            val player = offlinePlayer.uniqueId.let { plugin.playerRepository.getPlayer(it) }

            if (player == null) {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    sender.sendMessage(mmd("<red>Player not found in database</red>"))
                })
                return@Runnable
            }

            val lastSeen = formatDate("M/d/yyyy h:mm a", player.lastSeen * 1000L)
            Bukkit.getScheduler().runTask(plugin, Runnable {
                sender.sendMessage(mmd("<gray>${player.lastAccountName} was last seen on <white>$lastSeen</white></gray>"))
            })
        })
    }
}