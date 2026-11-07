package me.kyleseven.pixelessentials.commands.teleport

import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.database.models.StoredLocation
import me.kyleseven.pixelessentials.managers.teleport.Teleport
import me.kyleseven.pixelessentials.utils.mmd
import me.kyleseven.pixelessentials.utils.runTask
import me.kyleseven.pixelessentials.utils.runTaskAsync
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player

/**
 * Check whether a player is on teleport cooldown, and if so, tell [notify].
 *
 * @param player The player whose cooldown to check.
 * @param notify The player to send the cooldown message to.
 * @return True if the player is on cooldown.
 */
internal fun rejectIfOnCooldown(plugin: PixelEssentials, player: Player, notify: Player = player): Boolean {
    val remaining = plugin.teleportManager.remainingCooldown(player)
    if (remaining <= 0) return false

    if (notify == player) {
        notify.sendMessage(mmd("<red>You are on cooldown for another</red> <white>$remaining seconds</white><red>.</red>"))
    } else {
        notify.sendMessage(
            mmd(
                "<white><player></white> <red>is on cooldown for another</red> <white>$remaining seconds</white><red>.</red>",
                Placeholder.component("player", player.displayName())
            )
        )
    }
    return true
}

/**
 * Load a stored location off the main thread, then teleport the player to it.
 *
 * @param destinationName Shown to the player in the teleport messages.
 * @param notFound Sent to the player if [load] returns null.
 * @param load Loads the location. Runs asynchronously.
 */
internal fun teleportToStored(
    plugin: PixelEssentials,
    player: Player,
    destinationName: String,
    notFound: Component,
    load: () -> StoredLocation?
) {
    runTaskAsync(plugin) {
        val stored = load()

        runTask(plugin) {
            if (stored == null) {
                player.sendMessage(notFound)
                return@runTask
            }

            teleportToStored(plugin, player, destinationName, stored)
        }
    }
}

/**
 * Teleport the player to a stored location, or tell them if its world doesn't exist.
 */
internal fun teleportToStored(plugin: PixelEssentials, player: Player, destinationName: String, stored: StoredLocation) {
    val location = stored.toLocation() ?: run {
        player.sendMessage(
            mmd(
                "<red>World <white><world></white> does not exist.</red>",
                Placeholder.unparsed("world", stored.world)
            )
        )
        return
    }

    plugin.teleportManager.start(Teleport(player, Component.text(destinationName), { location }))
}