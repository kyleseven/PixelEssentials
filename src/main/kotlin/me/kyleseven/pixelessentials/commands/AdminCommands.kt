package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.kyleseven.pixelessentials.utils.mmd
import me.kyleseven.pixelessentials.utils.mms
import org.bukkit.entity.Player

class AdminCommands : BaseCommand() {
    @CommandAlias("invsee")
    @Description("Open the inventory of another player")
    @CommandCompletion("@players")
    @CommandPermission("pixelessentials.invsee")
    fun onInvsee(player: Player, target: OnlinePlayer) {
        player.sendMessage(mmd("<gray>Opening <white>${mms(target.player.displayName())}'s</white> inventory.</gray>"))
        player.openInventory(target.player.inventory)
    }

    @CommandAlias("enderchest|ec")
    @Description("Open the ender chest of another player")
    @CommandCompletion("@players")
    @CommandPermission("pixelessentials.enderchest")
    fun onEnderchest(player: Player, target: OnlinePlayer) {
        player.sendMessage(mmd("<gray>Opening <white>${mms(target.player.displayName())}'s</white> ender chest.</gray>"))
        player.openInventory(target.player.enderChest)
    }
}