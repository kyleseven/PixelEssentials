package me.kyleseven.pixelessentials.managers.teleport

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

/**
 * A teleport to be carried out by [TeleportManager].
 *
 * @property player The player being moved.
 * @property destinationName Shown to the player in the teleport messages.
 * @property destination Resolved when the teleport completes, so a moving target (another player) is followed.
 * Returns null if the destination no longer exists.
 * @property observer Another player to notify if the teleport is canceled, e.g. the other side of a /tpa.
 */
class Teleport(
    val player: Player,
    val destinationName: Component,
    val destination: () -> Location?,
    val observer: UUID? = null
)

enum class CancelReason {
    LOGGED_OFF,
    MOVED,
    DAMAGED,
    SUPERSEDED,
    DESTINATION_UNAVAILABLE
}