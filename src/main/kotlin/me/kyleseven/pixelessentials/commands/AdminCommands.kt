package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import io.papermc.paper.ban.BanListType
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.utils.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.ceil
import kotlin.math.floor

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

    @CommandAlias("more")
    @Description("Fill the held item to a full stack")
    @CommandPermission("pixelessentials.more")
    fun onMore(player: Player) {
        val itemInHand = player.inventory.itemInMainHand

        if (itemInHand.type.isAir) {
            player.sendMessage(mmd("<red>You must be holding an item to use this command.</red>"))
            return
        }

        val maxStackSize = itemInHand.type.maxStackSize
        itemInHand.amount = maxStackSize

        player.sendMessage(mmd("<gray>Stacked </gray><white>${mms(itemInHand.displayName())}</white><gray> to </gray><white>$maxStackSize</white><gray>.</gray>"))
    }

    @CommandAlias("sudo")
    @Description("Make another player run a command")
    @CommandCompletion("@players <command>")
    @CommandPermission("pixelessentials.sudo")
    fun onSudo(sender: CommandSender, target: OnlinePlayer, command: String) {
        val targetPlayer = target.player
        if (targetPlayer.hasPermission("pixelessentials.sudo.exempt")) {
            sender.sendMessage(mmd("<red>That player is exempt from sudo commands.</red>"))
            return
        }

        // Check if the command is a chat message
        if (command.startsWith("c:")) {
            val message = command.substring(2)
            targetPlayer.chat(message)
            return
        }

        targetPlayer.chat("/$command")
        sender.sendMessage(mmd("<gray>Forced <white>${targetPlayer.name}</white> to run <white>/$command</white>.</gray>"))
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
                val now = System.currentTimeMillis()
                val onlinePlayer = Bukkit.getPlayer(offlinePlayer.uniqueId)
                val sep = " <dark_gray>·</dark_gray> "

                // Relative time ("3d ago") with the exact date on hover
                val timeAgo = { timestamp: Long ->
                    "<hover:show_text:'${formatDate(DATE_FORMAT, timestamp)}'><white>" +
                            "${formatDuration(now - timestamp, maxUnits = 1)} ago</white></hover>"
                }

                // Clickable location that suggests a teleport command
                val locationValue = { worldName: String, x: Int, y: Int, z: Int ->
                    val text = "<white>$worldName $x, $y, $z</white>"
                    val worldKey = Bukkit.getWorld(worldName)?.key
                    if (worldKey == null) {
                        text
                    } else {
                        "<hover:show_text:'<gray>Click to teleport</gray>'>" +
                                "<click:suggest_command:'/execute in $worldKey run tp @s $x $y $z'>$text</click></hover>"
                    }
                }

                val lines = mutableListOf<Component>()

                val row = { content: String, resolvers: Array<TagResolver> ->
                    mmd("<gray>$content</gray>", *resolvers)
                }
                val addRow = { content: String -> lines += row(content, emptyArray()) }

                // Fixed title so the header never changes length
                lines += mmd(
                    "<gradient:dark_gray:gray>───────</gradient> <gradient:#ff7e5f:#feb47b>Player Info</gradient> " +
                            "<gradient:gray:dark_gray>───────</gradient>"
                )

                val status = if (onlinePlayer != null) "<green>● Online</green>" else "<dark_gray>● Offline</dark_gray>"
                val opTag = if (offlinePlayer.isOp) " <gold>[OP]</gold>" else ""
                addRow("Name: <white>${player.lastAccountName}</white>$opTag$sep$status")

                addRow(
                    "UUID: <hover:show_text:'<gray>Click to copy</gray>'>" +
                            "<click:copy_to_clipboard:'${player.uuid}'><white>${player.uuid}</white></click></hover>"
                )

                addRow(
                    "IP: <hover:show_text:'<white>${player.ipAddress}</white><newline><gray>Click to copy</gray>'>" +
                            "<click:copy_to_clipboard:'${player.ipAddress}'><white>" +
                            // Console can't hover, so show it the full IP
                            "${if (sender is Player) maskIp(player.ipAddress) else player.ipAddress}</white>" +
                            "</click></hover>"
                )

                val lastSeen = if (onlinePlayer != null) "<green>Online now</green>" else timeAgo(player.lastSeen * 1000L)
                addRow("First Join: ${timeAgo(player.firstJoin * 1000L)}${sep}Last Seen: $lastSeen")

                val playtime = String.format("%.2fh", player.totalPlaytime / 3600.0)
                addRow("Playtime: <white>$playtime</white>")

                // Ban status
                val banEntry = Bukkit.getBanList(BanListType.PROFILE)
                    .getBanEntry(Bukkit.createProfile(offlinePlayer.uniqueId))
                if (banEntry == null) {
                    addRow("Banned: <green>No</green>")
                } else {
                    val expiration = banEntry.expiration
                    val expiry = if (expiration == null) {
                        "<white>Permanent</white>"
                    } else {
                        "<hover:show_text:'${formatDate(DATE_FORMAT, expiration.time)}'><white>Expires in " +
                                "${formatDuration(expiration.time - now, maxUnits = 2)}</white></hover>"
                    }
                    val reason = banEntry.reason?.let { "$sep<white><reason></white>" } ?: ""
                    lines += row(
                        "Banned: <red>Yes</red>$reason$sep$expiry",
                        arrayOf(Placeholder.unparsed("reason", banEntry.reason ?: ""))
                    )
                }

                if (onlinePlayer != null) {
                    val loc = onlinePlayer.location
                    addRow("Location: ${locationValue(loc.world.name, loc.blockX, loc.blockY, loc.blockZ)}")

                    val gamemode = onlinePlayer.gameMode.name.lowercase().replaceFirstChar { it.uppercase() }
                    val health = ceil(onlinePlayer.health).toInt()
                    val maxHealth = onlinePlayer.getAttribute(Attribute.MAX_HEALTH)?.value?.toInt() ?: 20
                    addRow(
                        "<white>$gamemode</white>$sep<red>❤</red> <white>$health/$maxHealth</white>" +
                                "${sep}Hunger: <white>${onlinePlayer.foodLevel}/20</white>"
                    )
                } else if (lastLocation != null) {
                    val value = locationValue(
                        lastLocation.world,
                        floor(lastLocation.x).toInt(),
                        floor(lastLocation.y).toInt(),
                        floor(lastLocation.z).toInt()
                    )
                    addRow("Last Location: $value")
                }

                // Players get one message so chat can't interleave; console gets one per line
                // so each line gets its own log prefix
                if (sender is Player) {
                    sender.sendMessage(Component.join(JoinConfiguration.newlines(), lines))
                } else {
                    lines.forEach { sender.sendMessage(it) }
                }
            }
        }
    }

    /**
     * Mask the end of an IP address, e.g. `192.168.1.20` -> `192.168.x.x`.
     */
    private fun maskIp(ip: String): String {
        return when {
            ip.contains('.') -> ip.split('.').take(2).joinToString(".") + ".x.x"
            ip.contains(':') -> ip.split(':').take(2).joinToString(":") + ":x:x"
            else -> ip
        }
    }

    companion object {
        private const val DATE_FORMAT = "M/d/yyyy h:mm a"
    }
}