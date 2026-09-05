package me.kyleseven.pixelessentials.commands

import me.kyleseven.pixelessentials.managers.ScheduleTeleportResult
import me.kyleseven.pixelessentials.managers.TeleportMessages
import me.kyleseven.pixelessentials.managers.TeleportService
import org.bukkit.entity.Player

internal fun TeleportService.denyIfOnCooldown(player: Player): Boolean {
    val remaining = getRemainingCooldown(player)
    if (remaining == 0L) return false
    player.sendMessage(TeleportMessages.cooldown(remaining))
    return true
}

internal fun Player.report(result: ScheduleTeleportResult) {
    when (result) {
        is ScheduleTeleportResult.OnCooldown ->
            sendMessage(TeleportMessages.cooldown(result.remainingSeconds))
        ScheduleTeleportResult.PlayerOffline,
        ScheduleTeleportResult.Started,
        ScheduleTeleportResult.Completed,
        ScheduleTeleportResult.DestinationUnavailable,
        ScheduleTeleportResult.TeleportFailed -> Unit
    }
}
