package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import io.papermc.paper.ban.BanListType
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.utils.formatDate
import me.kyleseven.pixelessentials.utils.formatDuration
import me.kyleseven.pixelessentials.utils.mmd
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

    @CommandAlias("whois")
    @Description("Get information about a player")
    @CommandCompletion("@players")
    @CommandPermission("pixelessentials.whois")
    fun onWhois(sender: CommandSender, playerName: String) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val offlinePlayer = Bukkit.getOfflinePlayer(playerName)
            val player = plugin.playerRepository.getPlayer(offlinePlayer.uniqueId)
            if (player == null) {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    sender.sendMessage(mmd("<red>Player not found in database</red>"))
                })
                return@Runnable
            }

            val firstJoin = formatDate("M/d/yyyy h:mm a", player.firstJoin * 1000L)
            val lastSeen = formatDate("M/d/yyyy h:mm a", player.lastSeen * 1000L)
            val totalPlaytime = formatDuration(player.totalPlaytime * 1000L)

            Bukkit.getScheduler().runTask(plugin, Runnable {
                val banList = Bukkit.getBanList(BanListType.PROFILE)
                val banEntry = banList.getBanEntry(Bukkit.createProfile(offlinePlayer.uniqueId))
                val isBanned = banEntry != null
                val expirationDate = banEntry?.expiration

                val bannedStatus = if (isBanned) {
                    if (expirationDate != null) {
                        val formattedExpiration = formatDate("M/d/yyyy h:mm a", expirationDate.time)
                        "Yes (Expires: $formattedExpiration)"
                    } else {
                        "Yes"
                    }
                } else {
                    "No"
                }

                val opStatus = if (offlinePlayer.isOp) "Yes" else "No"

                val message = StringBuilder().apply {
                    appendLine("<gray>─────── Player Information ───────</gray>")
                    appendLine("<gray>Username: <white>${player.lastAccountName}</white></gray>")
                    appendLine("<gray>UUID: <white>${player.uuid}</white></gray>")
                    appendLine("<gray>OP: <white>$opStatus</white></gray>")
                    appendLine("<gray>First Join: <white>$firstJoin</white></gray>")
                    appendLine("<gray>Last Seen: <white>$lastSeen</white></gray>")
                    appendLine("<gray>Total Playtime: <white>$totalPlaytime</white></gray>")
                    appendLine("<gray>IP Address: <white>${player.ipAddress}</white></gray>")
                    appendLine("<gray>Banned: <white>$bannedStatus</white></gray>")
                    appendLine("<gray>Ban Reason: <white>${banEntry?.reason ?: "N/A"}</white></gray>")
                }

                // If the player is online, get additional information
                val onlinePlayer = Bukkit.getPlayer(offlinePlayer.uniqueId)
                if (onlinePlayer != null) {
                    val loc = onlinePlayer.location
                    val locationStr = "${loc.world.name} ${loc.blockX} ${loc.blockY} ${loc.blockZ}"
                    val gameModeStr = onlinePlayer.gameMode.name.lowercase().replaceFirstChar { it.uppercase() }
                    message.appendLine("<gray>Location: <white>$locationStr</white></gray>")
                    message.appendLine("<gray>Gamemode: <white>${gameModeStr}</white></gray>")
                    message.appendLine("<gray>Health: <white>${onlinePlayer.health.toInt()}/20</white></gray>")
                    message.appendLine("<gray>Hunger: <white>${onlinePlayer.foodLevel}/20</white></gray>")
                }

                sender.sendMessage(mmd(message.toString().trim()))
            })
        })
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

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val offlinePlayer = playerName.let { Bukkit.getOfflinePlayer(it) }
            val player = plugin.playerRepository.getPlayer(offlinePlayer.uniqueId)

            if (player == null) {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    sender.sendMessage(mmd("<red>Player not found in database</red>"))
                })
                return@Runnable
            }

            val lastSeenTimestamp = player.lastSeen * 1000L
            val lastSeen = "${
                formatDate(
                    "M/d/yyyy h:mm a",
                    lastSeenTimestamp
                )
            } (${formatDuration(System.currentTimeMillis() - lastSeenTimestamp)} ago)"
            Bukkit.getScheduler().runTask(plugin, Runnable {
                sender.sendMessage(mmd("<gray>${player.lastAccountName} was last seen on <white>$lastSeen</white></gray>"))
            })
        })
    }
}