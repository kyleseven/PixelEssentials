package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CatchUnknown
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import me.kyleseven.pixelessentials.PixelEssentials
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender

@CommandAlias("pixelessentials|pe")
class MainCommand(private val plugin: PixelEssentials) : BaseCommand() {
    @CatchUnknown
    @Subcommand("version")
    fun onVersion(sender: CommandSender) {
        @Suppress("DEPRECATION") // Description still applies to this plugin
        sender.sendMessage(
            MiniMessage.miniMessage()
                .deserialize("<gray>PixelEssentials <u>${plugin.description.version}</u></gray>")
        )
    }
}