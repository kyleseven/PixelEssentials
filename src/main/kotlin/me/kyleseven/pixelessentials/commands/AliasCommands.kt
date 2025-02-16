package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class AliasCommands : BaseCommand() {
    @CommandAlias("gmc")
    @Description("Switch to creative mode")
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
    @Description("Give an item to yourself")
    fun onI(player: Player, item: String, @Default("1") amount: Int) {
        val command = "give ${player.name} $item $amount"
        Bukkit.dispatchCommand(player, command)
    }
}