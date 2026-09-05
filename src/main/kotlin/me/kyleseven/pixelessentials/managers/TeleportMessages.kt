package me.kyleseven.pixelessentials.managers

import me.kyleseven.pixelessentials.utils.mmd
import me.kyleseven.pixelessentials.utils.mms
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object TeleportMessages {
    fun warmup(destination: String, seconds: Int): Component =
        mmd("<gray>Teleporting to</gray> <white>$destination</white> <gray>in $seconds seconds. Do not move.</gray>")

    fun teleported(destination: String): Component =
        mmd("<gray>Teleported to</gray> <white>$destination</white><gray>.</gray>")

    fun destinationUnavailable(): Component =
        mmd("<red>The teleport destination is no longer available.</red>")

    fun teleportFailed(): Component = mmd("<red>Teleport failed.</red>")

    fun replaced(): Component =
        mmd("<red>Previous teleport canceled due to new teleport request.</red>")

    fun canceledForPlayer(reason: TeleportCancellation): Component = when (reason) {
        TeleportCancellation.LOGGED_OFF -> mmd("<red>Teleport canceled because you logged off.</red>")
        TeleportCancellation.MOVED -> mmd("<red>Teleport canceled because you moved.</red>")
        TeleportCancellation.DAMAGED -> mmd("<red>Teleport canceled because you took damage.</red>")
    }

    fun canceledForObserver(player: Player, reason: TeleportCancellation): Component {
        val reasonText = when (reason) {
            TeleportCancellation.LOGGED_OFF -> "logged off"
            TeleportCancellation.MOVED -> "moved"
            TeleportCancellation.DAMAGED -> "took damage"
        }
        return mmd(
            "<red>Teleport from <white>${mms(player.displayName())}</white> was canceled because they $reasonText.</red>"
        )
    }

    fun cooldown(remainingSeconds: Long): Component =
        mmd("<red>You are on cooldown for another</red> <white>$remainingSeconds seconds</white><red>.</red>")

    fun playerCooldown(player: Player, remainingSeconds: Long): Component =
        mmd(
            "<white>${mms(player.displayName())}</white> <red>is on cooldown for another</red> " +
                    "<white>$remainingSeconds seconds</white><red>.</red>"
        )
}
