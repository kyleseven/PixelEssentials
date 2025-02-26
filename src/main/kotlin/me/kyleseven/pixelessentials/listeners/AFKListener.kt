package me.kyleseven.pixelessentials.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import me.kyleseven.pixelessentials.PixelEssentials
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class AFKListener(private val plugin: PixelEssentials) : Listener {
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.to.blockX != event.from.blockX || event.to.blockY != event.from.blockY || event.to.blockZ != event.from.blockZ) {
            plugin.afkManager.updateActivity(event.player)
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        plugin.afkManager.updateActivity(event.player)
    }

    @EventHandler
    fun onPlayerCommand(event: PlayerCommandPreprocessEvent) {
        if (!event.message.startsWith("/afk")) {
            plugin.afkManager.updateActivity(event.player)
        }
    }

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        plugin.afkManager.updateActivity(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        plugin.afkManager.removePlayer(event.player.uniqueId)
    }
}