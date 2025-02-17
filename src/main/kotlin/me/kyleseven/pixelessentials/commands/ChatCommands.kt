package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import me.kyleseven.pixelessentials.utils.mmd
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class ChatCommands : BaseCommand() {
    @CommandAlias("tell|msg|m|whisper")
    @Description("Send a private message to a player")
    @CommandCompletion("@players")
    @CommandPermission("pixelessentials.tell")
    fun onTell(sender: CommandSender, playerName: String, message: String) {
        val otherPlayer = Bukkit.getPlayer(playerName)
        if (otherPlayer == null) {
            sender.sendMessage(mmd("<red>Player not found</red>"))
            return
        }

        sender.sendMessage(mmd("<gray>[You -> <white>${otherPlayer.name}</white>]:</gray> $message"))
        otherPlayer.sendMessage(mmd("<gray>[<white>${sender.name}</white> -> You]:</gray> $message"))
    }
}