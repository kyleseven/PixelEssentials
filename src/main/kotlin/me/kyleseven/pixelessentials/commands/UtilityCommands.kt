package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.utils.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

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
        val target = if (playerName != null) Bukkit.getPlayer(playerName) else sender as? Player
        if (target == null) {
            sender.sendMessage(mmd("<red>Player not found</red>"))
            return
        }

        val ping = target.ping
        sender.sendMessage(mmd("<gray>${target.name}'s ping: <white>${ping}ms</white>"))
    }

    @CommandAlias("seen")
    @Description("Check when a player was last seen")
    @CommandCompletion("@players")
    @CommandPermission("pixelessentials.seen")
    fun onSeen(sender: CommandSender, playerName: String) {
        val onlinePlayer = Bukkit.getPlayerExact(playerName)
        if (onlinePlayer != null) {
            sender.sendMessage(mmd("<gray>${onlinePlayer.name} is currently <green>online</green>.</gray>"))
            return
        }

        runTaskAsync(plugin) {
            val offlinePlayer = playerName.let { Bukkit.getOfflinePlayer(it) }
            val player = plugin.playerRepository.getPlayer(offlinePlayer.uniqueId) ?: run {
                runTask(plugin) {
                    sender.sendMessage(mmd("<red>Player not found in database</red>"))
                }
                return@runTaskAsync
            }

            val lastSeenTimestamp = player.lastSeen * 1000L
            val lastSeen = "${
                formatDate(
                    "M/d/yyyy h:mm a",
                    lastSeenTimestamp
                )
            } (${formatDuration(System.currentTimeMillis() - lastSeenTimestamp)} ago)"

            runTask(plugin) {
                sender.sendMessage(mmd("<gray>${player.lastAccountName} was last seen on <white>$lastSeen</white></gray>"))
            }
        }
    }
}