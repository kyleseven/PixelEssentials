package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.kyleseven.pixelessentials.PixelEssentials
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender

@CommandAlias("pixelessentials|pe")
@Description("Main command for PixelEssentials")
class MainCommand(private val plugin: PixelEssentials) : BaseCommand() {
    @CatchUnknown
    @Subcommand("version")
    @Description("Displays the plugin version")
    fun onVersion(sender: CommandSender) {
        @Suppress("DEPRECATION") // Description still applies to this plugin
        sender.sendMessage(
            MiniMessage.miniMessage()
                .deserialize("<gray>PixelEssentials <u>${plugin.description.version}</u></gray>")
        )
    }

    @Subcommand("reload")
    @Description("Reloads the plugin configuration")
    @CommandPermission("pixelessentials.reload")
    fun onReload(sender: CommandSender) {
        plugin.reloadConfig()
        sender.sendMessage(
            MiniMessage.miniMessage()
                .deserialize("<green>PixelEssentials configuration reloaded!</green>")
        )
    }
}