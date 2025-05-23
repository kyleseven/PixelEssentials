package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.utils.mmd
import org.bukkit.command.CommandSender

@CommandAlias("pixelessentials|pe")
@Description("Main command for PixelEssentials")
class MainCommand(private val plugin: PixelEssentials) : BaseCommand() {
    private companion object {
        data class CommandHelp(val name: String, val description: String, val permissions: List<String>)

        const val HELP_PAGE_SIZE = 8
        val COMMAND_LIST = listOf(
            CommandHelp("/afk", "Toggle your AFK status.", listOf("pixelessentials.afk")),
            CommandHelp("/tpa", "Request to teleport to a player.", listOf("pixelessentials.tpa")),
            CommandHelp("/tpahere", "Request a player to teleport to you.", listOf("pixelessentials.tpahere")),
            CommandHelp("/tpaall", "Request all players to teleport to you.", listOf("pixelessentials.tpaall")),
            CommandHelp("/tpall", "Teleport all players to you immediately.", listOf("pixelessentials.tpall")),
            CommandHelp("/tpaccept", "Accept a teleport request.", listOf("pixelessentials.tpaccept")),
            CommandHelp("/tpdeny", "Deny a teleport request.", listOf("pixelessentials.tpdeny")),
            CommandHelp("/tpacancel", "Cancel an outgoing teleport request.", listOf("pixelessentials.tpacancel")),
            CommandHelp(
                "/back",
                "Teleport to your previous location.",
                listOf("pixelessentials.back", "pixelessentials.back.onteleport", "pixelessentials.back.ondeath")
            ),
            CommandHelp("/home", "Teleport to your home location.", listOf("pixelessentials.home")),
            CommandHelp("/sethome", "Set a home location.", listOf("pixelessentials.sethome")),
            CommandHelp("/delhome", "Delete your home location.", listOf("pixelessentials.sethome")),
            CommandHelp("/warp", "Teleport to a warp location.", listOf("pixelessentials.warp")),
            CommandHelp("/setwarp", "Set a warp location.", listOf("pixelessentials.setwarp")),
            CommandHelp("/delwarp", "Delete a warp location.", listOf("pixelessentials.delwarp")),
            CommandHelp("/spawn", "Teleport to spawn.", listOf("pixelessentials.spawn")),
            CommandHelp("/setspawn", "Set the spawn location.", listOf("pixelessentials.setspawn")),
            CommandHelp("/delspawn", "Delete the spawn location.", listOf("pixelessentials.delspawn")),
            CommandHelp("/msg", "Send a private message to a player.", listOf("pixelessentials.msg")),
            CommandHelp("/reply", "Reply to the last player who messaged you.", listOf("pixelessentials.msg")),
            CommandHelp("/motd", "See the message of the day.", listOf("pixelessentials.motd")),
            CommandHelp("/rules", "See the server rules.", listOf("pixelessentials.rules")),
            CommandHelp("/list", "See a list of all online players.", listOf("pixelessentials.list")),
            CommandHelp("/ping", "See the ping of yourself or another player.", listOf("pixelessentials.ping")),
            CommandHelp("/seen", "See when a player was last online.", listOf("pixelessentials.seen")),
            CommandHelp(
                "/whois",
                "See various info about a player, including UUID, IP, Ban Status.",
                listOf("pixelessentials.whois")
            ),
            CommandHelp(
                "/invsee", "Open the inventory of another player.", listOf("pixelessentials.invsee")
            ),
            CommandHelp(
                "/enderchest", "Open the ender chest of another player.", listOf("pixelessentials.enderchest")
            ),
            CommandHelp(
                "/sudo", "Force another player to run a command.", listOf("pixelessentials.sudo")
            ),
            CommandHelp(
                "/playtime", "See your own or another player's playtime.", listOf("pixelessentials.playtime")
            ),
            CommandHelp(
                "/playtimetop",
                "Show a leaderboard of players ordered by playtime.",
                listOf("pixelessentials.playtimetop")
            ),
        )
    }

    @Subcommand("help")
    @Description("Get a list of PixelEssentials commands")
    fun onHelp(sender: CommandSender, @Default("1") page: Int) {
        val accessibleCommands = COMMAND_LIST.filter { command ->
            command.permissions.any { sender.hasPermission(it) }
        }

        val pageCount = Math.ceilDiv(accessibleCommands.size, HELP_PAGE_SIZE)

        if (page < 1 || page > pageCount) {
            sender.sendMessage(mmd("<red>Invalid page number. Please use a number between 1 and $pageCount.</red>"))
            return
        }

        sender.sendMessage(mmd("<gradient:dark_gray:gray>───────</gradient> <gradient:#ff7e5f:#feb47b>PixelEssentials Help</gradient> <gradient:gray:dark_gray>───────</gradient>"))

        val startIndex = (page - 1) * HELP_PAGE_SIZE
        val endIndex = minOf(startIndex + HELP_PAGE_SIZE, accessibleCommands.size)

        for (i in startIndex until endIndex) {
            val command = accessibleCommands[i]
            sender.sendMessage(mmd("<gradient:#ff7e5f:#feb47b>${command.name}</gradient> <gray>:</gray> <white>${command.description}</white>"))
        }

        if (pageCount > 1) {
            val previousComponent = if (page > 1) {
                "<gradient:#ff7e5f:#feb47b><hover:show_text:'/pe help ${page - 1}'><click:run_command:'/pe help ${page - 1}'>«</click></hover></gradient>"
            } else {
                "<gradient:gray:dark_gray>«</gradient>"
            }

            val nextComponent = if (page < pageCount) {
                "<gradient:#ff7e5f:#feb47b><hover:show_text:'/pe help ${page + 1}'><click:run_command:'/pe help ${page + 1}'>»</click></hover></gradient>"
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

    @Subcommand("version")
    @Description("Displays the plugin version")
    fun onVersion(sender: CommandSender) {
        @Suppress("DEPRECATION") // Description still applies to this plugin
        sender.sendMessage(mmd("<gray>PixelEssentials <white>${plugin.description.version}</white> by PixelArray</gray>"))
    }

    @Subcommand("reload")
    @Description("Reloads the plugin configuration")
    @CommandPermission("pixelessentials.reload")
    fun onReload(sender: CommandSender) {
        plugin.reloadConfig()
        sender.sendMessage(mmd("<green>PixelEssentials configuration reloaded!</green>"))
    }

    @CatchUnknown
    fun onUnknown(sender: CommandSender) {
        sender.sendMessage(mmd("<red>Unknown command. Use <white>/pe help</white> for a list of commands.</red>"))
    }
}