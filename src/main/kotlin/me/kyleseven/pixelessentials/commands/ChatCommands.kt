package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import me.kyleseven.pixelessentials.utils.mmd
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import java.time.Instant

class ChatCommands : BaseCommand() {
    private data class MessageData(val senderName: String, val timestamp: Instant)

    private val lastReceivedMessages = hashMapOf<String, MessageData>()
    private val replyTimeout = 300L
    private val consoleName = Bukkit.getConsoleSender().name

    private fun isMessageExpired(messageData: MessageData): Boolean {
        return Instant.now().epochSecond - messageData.timestamp.epochSecond > replyTimeout
    }

    private fun sendPrivateMessage(sender: CommandSender, receiver: CommandSender, message: String) {
        val senderName = if (sender is ConsoleCommandSender) consoleName else sender.name
        val receiverName = if (receiver is ConsoleCommandSender) consoleName else receiver.name

        sender.sendMessage(mmd("<gray>[You -> <white>$receiverName</white>]:</gray> $message"))
        receiver.sendMessage(mmd("<gray>[<white>$senderName</white> -> You]:</gray> $message"))

        val trackingKey = if (receiver is ConsoleCommandSender) consoleName else receiver.name
        lastReceivedMessages[trackingKey] = MessageData(senderName, Instant.now())
    }

    @CommandAlias("msg|m|whisper|w|tell")
    @Description("Send a private message to a player")
    @CommandCompletion("@players")
    @CommandPermission("pixelessentials.msg")
    fun onMsg(sender: CommandSender, targetName: String, message: String) {
        val target = when (targetName.lowercase()) {
            "console" -> Bukkit.getConsoleSender()
            else -> Bukkit.getPlayer(targetName) ?: run {
                sender.sendMessage(mmd("<red>Player not found</red>"))
                return
            }
        }

        sendPrivateMessage(sender, target, message)
    }

    @CommandAlias("reply|r")
    @Description("Reply to the last player who messaged you")
    @CommandPermission("pixelessentials.msg")
    fun onReply(sender: CommandSender, message: String) {
        val trackingKey = if (sender is ConsoleCommandSender) consoleName else sender.name
        val lastMessageData = lastReceivedMessages[trackingKey]

        if (lastMessageData == null || isMessageExpired(lastMessageData)) {
            sender.sendMessage(mmd("<red>Nobody has messaged you recently.</red>"))
            lastReceivedMessages.remove(trackingKey)
            return
        }

        val target = when (lastMessageData.senderName) {
            consoleName -> Bukkit.getConsoleSender()
            else -> Bukkit.getPlayer(lastMessageData.senderName) ?: run {
                sender.sendMessage(mmd("<red>Sender is no longer available.</red>"))
                return
            }
        }

        sendPrivateMessage(sender, target, message)
    }
}