package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import me.kyleseven.pixelessentials.managers.TeleportPlan
import me.kyleseven.pixelessentials.managers.TeleportService
import me.kyleseven.pixelessentials.utils.mmd
import org.bukkit.entity.Player

class BackCommands(private val teleports: TeleportService) : BaseCommand() {
    @CommandAlias("back")
    @Description("Teleport to your previous location")
    @CommandPermission("pixelessentials.back")
    fun onBack(player: Player) {
        if (teleports.denyIfOnCooldown(player)) return

        val backLocation = teleports.getBackLocation(player)
        if (backLocation == null) {
            player.sendMessage(mmd("<red>You don't have a location to go back to.</red>"))
            return
        }

        player.report(
            teleports.schedule(
                TeleportPlan(
                    player = player,
                    destination = { backLocation },
                    destinationName = "previous location"
                )
            )
        )
    }
}
