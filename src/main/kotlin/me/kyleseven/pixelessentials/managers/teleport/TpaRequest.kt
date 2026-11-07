package me.kyleseven.pixelessentials.managers.teleport

import org.bukkit.entity.Player
import java.util.*

enum class TpaType {
    /** The requester teleports to the target. */
    TPA,

    /** The target teleports to the requester. */
    TPAHERE
}

data class TpaRequest(
    val requester: UUID,
    val target: UUID,
    val type: TpaType
)

/**
 * The result of a target taking their pending request to accept or deny it.
 */
sealed interface IncomingRequest {
    data object None : IncomingRequest
    data object RequesterOffline : IncomingRequest

    data class Found(val requester: Player, val target: Player, val type: TpaType) : IncomingRequest {
        fun toTeleport(): Teleport {
            val (mover, destination) = when (type) {
                TpaType.TPA -> requester to target
                TpaType.TPAHERE -> target to requester
            }

            return Teleport(
                player = mover,
                destinationName = destination.displayName(),
                destination = { destination.takeIf { it.isOnline }?.location },
                observer = destination.uniqueId
            )
        }
    }
}