package me.kyleseven.pixelessentials.commands.teleport

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.managers.teleport.Teleport
import me.kyleseven.pixelessentials.utils.mmd
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class BackCommand(private val plugin: PixelEssentials) : BaseCommand() {
    @CommandAlias("back")
    @Description("Teleport to your previous location")
    @CommandPermission("pixelessentials.back")
    fun onBack(player: Player) {
        if (rejectIfOnCooldown(plugin, player)) return

        val backLocation = plugin.teleportManager.getBackLocation(player) ?: run {
            player.sendMessage(mmd("<red>You don't have a location to go back to.</red>"))
            return
        }

        plugin.teleportManager.start(
            Teleport(
                player = player,
                destinationName = Component.text("previous location"),
                destination = { backLocation.takeIf { it.isWorldLoaded } }
            )
        )
    }
}