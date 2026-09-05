package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import me.kyleseven.pixelessentials.managers.TeleportPlan
import me.kyleseven.pixelessentials.managers.TeleportPolicy
import me.kyleseven.pixelessentials.managers.TeleportService
import me.kyleseven.pixelessentials.managers.ScheduleTeleportResult
import me.kyleseven.pixelessentials.utils.mmd
import me.kyleseven.pixelessentials.utils.mms
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class TeleportAdminCommands(private val teleports: TeleportService) : BaseCommand() {
    @CommandAlias("tpall")
    @Description("Teleport all players to you")
    @CommandPermission("pixelessentials.tpall")
    fun onTpall(player: Player) {
        val targets = Bukkit.getOnlinePlayers()
            .filter { it.uniqueId != player.uniqueId }
        val successCount = targets.count { target ->
            teleports.schedule(
                    TeleportPlan(
                        player = target,
                        destination = { player.takeIf(Player::isOnline)?.location },
                        destinationName = mms(player.displayName()),
                        observer = player
                    ),
                    TeleportPolicy.ADMIN
                ) == ScheduleTeleportResult.Completed
        }

        val message = when {
            targets.isEmpty() -> "<gray>There are no other players to teleport.</gray>"
            successCount == targets.size -> "<gray>All players have been teleported to you.</gray>"
            else -> "<gray>Teleported <white>$successCount</white> of <white>${targets.size}</white> players to you.</gray>"
        }
        player.sendMessage(mmd(message))
    }
}
