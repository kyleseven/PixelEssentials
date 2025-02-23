package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import io.papermc.paper.ban.BanListType
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.utils.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AdminCommands(private val plugin: PixelEssentials) : BaseCommand() {
    @CommandAlias("invsee")
    @Description("Open the inventory of another player")
    @CommandCompletion("@players")
    @CommandPermission("pixelessentials.invsee")
    fun onInvsee(player: Player, target: OnlinePlayer) {
        player.sendMessage(mmd("<gray>Opening <white>${mms(target.player.displayName())}'s</white> inventory.</gray>"))
        player.openInventory(target.player.inventory)
    }

    @CommandAlias("enderchest|ec")
    @Description("Open the ender chest of another player")
    @CommandCompletion("@players")
    @CommandPermission("pixelessentials.enderchest")
    fun onEnderchest(player: Player, @Optional target: OnlinePlayer?) {
        if (target == null) {
            player.sendMessage(mmd("<gray>Opening your ender chest.</gray>"))
            player.openInventory(player.enderChest)
            return
        }

        player.sendMessage(mmd("<gray>Opening <white>${mms(target.player.displayName())}'s</white> ender chest.</gray>"))
        player.openInventory(target.player.enderChest)
    }

    @CommandAlias("whois")
    @Description("Get information about a player")
    @CommandCompletion("@players")
    @CommandPermission("pixelessentials.whois")
    fun onWhois(sender: CommandSender, playerName: String) {
        runTaskAsync(plugin) {
            val offlinePlayer = Bukkit.getOfflinePlayer(playerName)
            val player = plugin.playerRepository.getPlayer(offlinePlayer.uniqueId)
            if (player == null) {
                runTask(plugin) {
                    sender.sendMessage(mmd("<red>Player not found in database</red>"))
                }
                return@runTaskAsync
            }

            val firstJoin = formatDate("M/d/yyyy h:mm a", player.firstJoin * 1000L)
            val lastSeen = formatDate("M/d/yyyy h:mm a", player.lastSeen * 1000L)
            val totalPlaytime = formatDuration(player.totalPlaytime * 1000L)

            runTask(plugin) {
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

                message.lines().forEach { sender.sendMessage(mmd(it)) }
            }
        }
    }
}