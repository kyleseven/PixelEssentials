package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import me.kyleseven.pixelessentials.utils.mmd
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.time.Instant

class ChatCommands : BaseCommand() {
    private data class MessageData(val senderName: String, val timestamp: Instant)

    private val lastReceivedMessages = hashMapOf<String, MessageData>()
    private val replyTimeout = 300L

    private fun isMessageExpired(messageData: MessageData): Boolean {
        return Instant.now().epochSecond - messageData.timestamp.epochSecond > replyTimeout
    }

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

        lastReceivedMessages[otherPlayer.name] = MessageData(sender.name, Instant.now())

        sender.sendMessage(mmd("<gray>[You -> <white>${otherPlayer.name}</white>]:</gray> $message"))
        otherPlayer.sendMessage(mmd("<gray>[<white>${sender.name}</white> -> You]:</gray> $message"))
    }

    @CommandAlias("reply|r")
    @Description("Reply to the last player who messaged you")
    @CommandPermission("pixelessentials.tell")
    fun onReply(sender: CommandSender, message: String) {
        val lastMessageData = lastReceivedMessages[sender.name]
        if (lastMessageData == null || isMessageExpired(lastMessageData)) {
            sender.sendMessage(mmd("<red>Nobody has messaged you recently.</red>"))
            lastReceivedMessages.remove(sender.name)
            return
        }

        val otherPlayer = Bukkit.getPlayer(lastMessageData.senderName)
        if (otherPlayer == null) {
            sender.sendMessage(mmd("<red>Player not found.</red>"))
            return
        }

        sender.sendMessage(mmd("<gray>[You -> <white>${otherPlayer.name}</white>]:</gray> $message"))
        otherPlayer.sendMessage(mmd("<gray>[<white>${sender.name}</white> -> You]:</gray> $message"))
    }
}