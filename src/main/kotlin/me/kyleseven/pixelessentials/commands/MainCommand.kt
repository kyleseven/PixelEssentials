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

        val COMMAND_LIST = listOf(
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
            CommandHelp("/motd", "See the message of the day.", listOf("pixelessentials.motd")),
            CommandHelp("/list", "See a list of all online players.", listOf("pixelessentials.list")),
            CommandHelp("/ping", "See the ping of yourself or another player.", listOf("pixelessentials.ping")),
            CommandHelp("/seen", "See when a player was last online.", listOf("pixelessentials.seen")),
            CommandHelp(
                "/whois",
                "See various info about a player, including UUID, IP, Ban Status.",
                listOf("pixelessentials.whois")
            )
        )
    }

    @Subcommand("help")
    @Description("Get a list of PixelEssentials commands")
    fun onHelp(sender: CommandSender) {
        sender.sendMessage(mmd("<gradient:dark_gray:gray>───────</gradient> <gradient:#ff7e5f:#feb47b>PixelEssentials Help</gradient> <gradient:gray:dark_gray>───────</gradient>"))

        COMMAND_LIST.forEach { command ->
            if (command.permissions.any { sender.hasPermission(it) }) {
                sender.sendMessage(mmd("<gradient:#ff7e5f:#feb47b>${command.name}</gradient> <gray>:</gray> <white>${command.description}</white>"))
            }
        }
    }

    @Subcommand("version")
    @Description("Displays the plugin version")
    fun onVersion(sender: CommandSender) {
        @Suppress("DEPRECATION") // Description still applies to this plugin
        sender.sendMessage(mmd("<gray>PixelEssentials <u>${plugin.description.version}</u></gray>"))
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