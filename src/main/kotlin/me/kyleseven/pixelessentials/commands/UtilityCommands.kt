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

    @CommandAlias("playtimetop")
    @Description("Show a leaderboard of players ordered by play time")
    @CommandPermission("pixelessentials.playtimetop")
    fun onPlaytimeTop(sender: CommandSender, @Default("1") page: Int) {
        val pageSize = 8
        runTaskAsync(plugin) {
            val pageCount = Math.ceilDiv(plugin.playerRepository.getPlayerCount(), pageSize)
            if (page < 1 || page > pageCount) {
                runTask(plugin) {
                    sender.sendMessage(mmd("<red>Invalid page number</red>"))
                }
                return@runTaskAsync
            }

            val players = plugin.playerRepository.getPlaytimeLeaderboard(page, pageSize)
            val startRank = (page - 1) * pageSize + 1

            runTask(plugin) {
                sender.sendMessage(
                    mmd(
                        "<gradient:dark_gray:gray>───────</gradient> <gradient:#ff7e5f:#feb47b>Playtime Top</gradient> <gradient:gray:dark_gray>───────</gradient>"
                    )
                )

                players.forEachIndexed { index, player ->
                    val rank = startRank + index
                    val playtime = String.format("%.2f hours", player.totalPlaytime / 3600.0)
                    sender.sendMessage(
                        mmd(
                            "<gradient:#ff7e5f:#feb47b>$rank.</gradient> <gray>${player.lastAccountName}</gray> <dark_gray>:</dark_gray> <white>$playtime</white>"
                        )
                    )
                }

                val previousComponent = if (page > 1) {
                    "<gradient:#ff7e5f:#feb47b><hover:show_text:'/playtimetop ${page - 1}'><click:run_command:'/playtimetop ${page - 1}'>«</click></hover></gradient>"
                } else {
                    "<gradient:gray:dark_gray>«</gradient>"
                }

                val nextComponent = if (page < pageCount) {
                    "<gradient:#ff7e5f:#feb47b><hover:show_text:'/playtimetop ${page + 1}'><click:run_command:'/playtimetop ${page + 1}'>»</click></hover></gradient>"
                } else {
                    "<gradient:gray:dark_gray>»</gradient>"
                }

                sender.sendMessage(
                    mmd(
                        "<gradient:dark_gray:gray>──────</gradient> $previousComponent <gradient:#ff7e5f:#feb47b>Page $page of $pageCount</gradient> $nextComponent <gradient:gray:dark_gray>──────</gradient>"
                    )
                )
            }
        }
    }
}