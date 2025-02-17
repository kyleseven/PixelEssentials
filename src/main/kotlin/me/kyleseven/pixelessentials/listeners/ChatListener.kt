package me.kyleseven.pixelessentials.listeners

import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.utils.mmd
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatListener(private val plugin: PixelEssentials) : Listener, ChatRenderer.ViewerUnaware {
    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        if (!plugin.configProvider.customChatEnabled) return

        event.renderer(ChatRenderer.viewerUnaware(this))
    }

    override fun render(source: Player, sourceDisplayName: Component, message: Component): Component {
        val format = plugin.configProvider.customChatFormat

        val prefix = plugin.vaultChat.getPlayerPrefix(source)
        val username = source.name
        val rawMessage = LegacyComponentSerializer.legacySection().serialize(message)

        val formattedMessage = format.replace("{prefix}", prefix)
            .replace("{username}", username)
            .replace("{message}", rawMessage)

        return mmd(formattedMessage)
    }
}