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
            val player = plugin.playerRepository.getPlayer(offlinePlayer.uniqueId) ?: run {
                runTask(plugin) {
                    sender.sendMessage(mmd("<red>Player not found in database</red>"))
                }
                return@runTaskAsync
            }

            val lastLocation = plugin.playerRepository.getPlayerLastLocation(offlinePlayer.uniqueId)

            runTask(plugin) {
                val formatKeyValue = { key: String, value: Any? ->
                    "<gray>$key: <white>${value ?: "N/A"}</white></gray>"
                }

                val banList = Bukkit.getBanList(BanListType.PROFILE)
                val banEntry = banList.getBanEntry(Bukkit.createProfile(offlinePlayer.uniqueId))

                val bannedStatus = if (banEntry == null) {
                    "No"
                } else {
                    val expiration = banEntry.expiration
                    if (expiration != null) {
                        val formattedExpiration = formatDate("M/d/yyyy h:mm a", expiration.time)
                        "Yes (Expires: $formattedExpiration)"
                    } else {
                        "Yes"
                    }
                }

                val infoLines = mutableListOf(
                    "<gray>─────── Player Information ───────</gray>",
                    formatKeyValue("Username", player.lastAccountName),
                    formatKeyValue("UUID", player.uuid),
                    formatKeyValue("OP", if (offlinePlayer.isOp) "Yes" else "No"),
                    formatKeyValue("First Join", formatDate("M/d/yyyy h:mm a", player.firstJoin * 1000L)),
                    formatKeyValue("Last Seen", formatDate("M/d/yyyy h:mm a", player.lastSeen * 1000L)),
                    formatKeyValue("Total Playtime", formatDuration(player.totalPlaytime * 1000L)),
                    formatKeyValue("IP Address", player.ipAddress),
                    formatKeyValue("Banned", bannedStatus),
                    formatKeyValue("Ban Reason", banEntry?.reason ?: "N/A")
                )

                val onlinePlayer = Bukkit.getPlayer(offlinePlayer.uniqueId)
                if (onlinePlayer != null) {
                    // Player is online
                    val loc = onlinePlayer.location
                    infoLines.addAll(
                        listOf(
                            formatKeyValue("Location", "${loc.world.name} ${loc.blockX} ${loc.blockY} ${loc.blockZ}"),
                            formatKeyValue(
                                "Gamemode", onlinePlayer.gameMode.name.lowercase().replaceFirstChar { it.uppercase() }),
                            formatKeyValue("Health", "${onlinePlayer.health.toInt()}/20"),
                            formatKeyValue("Hunger", "${onlinePlayer.foodLevel}/20")
                        )
                    )
                } else {
                    // Player is offline
                    if (lastLocation != null) {
                        infoLines.add(
                            formatKeyValue(
                                "Last Location",
                                "${lastLocation.world} ${lastLocation.x.toInt()} ${lastLocation.y.toInt()} ${lastLocation.z.toInt()}"
                            )
                        )
                    }
                }

                infoLines.forEach { sender.sendMessage(mmd(it)) }
            }
        }
    }
}