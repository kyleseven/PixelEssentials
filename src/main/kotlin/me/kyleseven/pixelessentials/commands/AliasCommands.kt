package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.kyleseven.pixelessentials.utils.mmd
import org.bukkit.Bukkit
import org.bukkit.command.CommandException
import org.bukkit.entity.Player

class AliasCommands : BaseCommand() {
    @CommandAlias("gmc")
    @Description("Switch to creative mode")
    @CommandPermission("minecraft.command.gamemode")
    @CommandCompletion("@players")
    fun onGmc(player: Player, @Optional targetPlayer: String?) {
        val command = if (targetPlayer != null) {
            "gamemode creative $targetPlayer"
        } else {
            "gamemode creative"
        }
        Bukkit.dispatchCommand(player, command)
    }

    @CommandAlias("gms")
    @Description("Switch to survival mode")
    @CommandPermission("minecraft.command.gamemode")
    @CommandCompletion("@players")
    fun onGms(player: Player, @Optional targetPlayer: String?) {
        val command = if (targetPlayer != null) {
            "gamemode survival $targetPlayer"
        } else {
            "gamemode survival"
        }
        Bukkit.dispatchCommand(player, command)
    }

    @CommandAlias("gmsp")
    @Description("Switch to spectator mode")
    @CommandPermission("minecraft.command.gamemode")
    @CommandCompletion("@players")
    fun onGmsp(player: Player, @Optional targetPlayer: String?) {
        val command = if (targetPlayer != null) {
            "gamemode spectator $targetPlayer"
        } else {
            "gamemode spectator"
        }
        Bukkit.dispatchCommand(player, command)
    }

    @CommandAlias("gma")
    @Description("Switch to adventure mode")
    @CommandPermission("minecraft.command.gamemode")
    @CommandCompletion("@players")
    fun onGma(player: Player, @Optional targetPlayer: String?) {
        val command = if (targetPlayer != null) {
            "gamemode adventure $targetPlayer"
        } else {
            "gamemode adventure"
        }
        Bukkit.dispatchCommand(player, command)
    }

    @CommandAlias("i")
    @CommandCompletion("@materials")
    @CommandPermission("minecraft.command.give")
    @Description("Give an item to yourself")
    fun onI(player: Player, item: String, @Default("1") amount: Int) {
        val command = "give ${player.name} $item $amount"
        try {
            if (!Bukkit.dispatchCommand(player, command)) {
                player.sendMessage(mmd("<red>Unable to give <white>$item</white>.</red>"))
            }
        } catch (_: CommandException) {
            player.sendMessage(
                mmd("<red>Unknown or invalid item <white>$item</white>. Use a valid Minecraft item ID.</red>")
            )
        }
    }

    @CommandAlias("tphere")
    @Description("Teleport a player to you")
    @CommandCompletion("@players")
    @CommandPermission("minecraft.command.tp")
    fun onTphere(player: Player, playerName: String) {
        val command = "tp $playerName ${player.name}"
        Bukkit.dispatchCommand(player, command)
    }
}
