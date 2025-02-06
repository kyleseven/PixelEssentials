package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.database.models.PlayerHome
import me.kyleseven.pixelessentials.database.models.Warp
import me.kyleseven.pixelessentials.utils.TeleportRequest
import me.kyleseven.pixelessentials.utils.mmd
import me.kyleseven.pixelessentials.utils.mms
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

class TeleportCommands(private val plugin: PixelEssentials) : BaseCommand() {
    private fun isSelfTeleport(player: Player, target: Player): Boolean {
        if (player.uniqueId == target.uniqueId) {
            player.sendMessage(mmd("<red>You can't teleport to yourself.</red>"))
            return true
        }
        return false
    }

    private fun sendTpRequestMessage(sender: Player, receiver: Player, isTpahere: Boolean) {
        val requestType = if (isTpahere) "wants you to teleport to them" else "wants to teleport to you"
        receiver.sendMessage(
            mmd(
                "<white>${mms(sender.displayName())}</white> <gray>$requestType.</gray>\n" +
                        "<gray>Use</gray> <hover:show_text:'<green>Click to accept request.</green>'>" +
                        "<click:run_command:'/tpaccept'><green>/tpaccept</green></click></hover> " +
                        "<gray>to accept this request.</gray>\n" +
                        "<gray>Use</gray> <hover:show_text:'<red>Click to deny request.</red>'>" +
                        "<click:run_command:'/tpdeny'><red>/tpdeny</red></click></hover> " +
                        "<gray>to deny this request.</gray>"
            )
        )
    }

    @CommandAlias("tpa")
    @Description("Request to teleport to a player")
    @CommandPermission("pixelessentials.tpa")
    @CommandCompletion("@players")
    fun onTpa(player: Player, target: OnlinePlayer) {
        if (isSelfTeleport(player, target.player)) return

        if (plugin.teleportManager.isOnCooldown(player)) {
            player.sendMessage(
                mmd(
                    "<red>You are on cooldown for another</red> <white>${
                        plugin.teleportManager.getRemainingCooldown(
                            player
                        )
                    } seconds</white><red>.</red>"
                )
            )
            return
        }

        if (!plugin.teleportManager.addRequest(player, target.player, false)) {
            return
        }

        player.sendMessage(
            mmd("<gray>Sent teleport request to</gray> <white>${mms(target.player.displayName())}</white><gray>.</gray>")
        )
        sendTpRequestMessage(player, target.player, false)
    }

    @CommandAlias("tpahere")
    @Description("Request a player to teleport to you")
    @CommandPermission("pixelessentials.tpahere")
    @CommandCompletion("@players")
    fun onTpahere(player: Player, target: OnlinePlayer) {
        if (isSelfTeleport(player, target.player)) return

        if (plugin.teleportManager.isOnCooldown(target.player)) {
            player.sendMessage(
                mmd(
                    "<white>${mms(target.player.displayName())}</white> is on cooldown for another <white>${
                        plugin.teleportManager.getRemainingCooldown(
                            target.player
                        )
                    } seconds</white>."
                )
            )
            return
        }

        if (!plugin.teleportManager.addRequest(player, target.player, true)) {
            return
        }

        player.sendMessage(
            mmd("<gray>Sent teleport request to</gray> <white>${mms(target.player.displayName())}</white><gray>.</gray>")
        )
        sendTpRequestMessage(player, target.player, true)
    }

    @CommandAlias("tpaall")
    @Description("Request all players to teleport to you")
    @CommandPermission("pixelessentials.tpaall")
    fun onTpaall(player: Player) {
        var successCount = 0
        Bukkit.getOnlinePlayers().forEach {
            if (it != player && plugin.teleportManager.addRequest(player, it, true)) {
                sendTpRequestMessage(player, it, true)
                successCount++
            }
        }

        when (successCount) {
            0 -> player.sendMessage(mmd("<red>No players to send requests to.</red>"))
            1 -> player.sendMessage(mmd("<gray>Sent request to 1 player.</gray>"))
            else -> player.sendMessage(
                mmd("<gray>Sent request to $successCount players.</gray>")
            )
        }
    }

    @CommandAlias("tpall")
    @Description("Teleport all players to you")
    @CommandPermission("pixelessentials.tpall")
    fun onTpall(player: Player) {
        plugin.teleportManager.processTpall(player)
    }

    @CommandAlias("tpaccept")
    @Description("Accept a teleport request")
    fun onTpaccept(player: Player) {
        if (!plugin.teleportManager.acceptRequest(player)) {
            player.sendMessage(
                mmd("<gray>You have no pending teleport requests.</gray>")
            )
        }
    }

    @CommandAlias("tpdeny")
    @Description("Deny a teleport request")
    fun onTpdeny(player: Player) {
        if (!plugin.teleportManager.denyRequest(player)) {
            player.sendMessage(
                mmd("<gray>You have no pending teleport requests.</gray>")
            )
        }
    }

    @CommandAlias("tpacancel")
    @Description("Cancel a teleport request")
    fun onTpacancel(player: Player) {
        plugin.teleportManager.cancelRequest(player)
    }

    @CommandAlias("sethome")
    @Description("Set your home location")
    @CommandPermission("pixelessentials.sethome")
    fun onSethome(player: Player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            plugin.playerRepository.upsertPlayerHome(
                player.uniqueId, PlayerHome(
                    x = player.location.x,
                    y = player.location.y,
                    z = player.location.z,
                    pitch = player.location.pitch.toDouble(),
                    yaw = player.location.yaw.toDouble(),
                    world = player.location.world.name
                )
            )

            Bukkit.getScheduler().runTask(plugin, Runnable {
                player.sendMessage(mmd("<gray>Your home location has been set.</gray>"))
            })
        })
    }

    @CommandAlias("delhome")
    @Description("Delete your home location")
    @CommandPermission("pixelessentials.sethome")
    fun onDelhome(player: Player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            plugin.playerRepository.getPlayerHome(player.uniqueId) ?: run {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    player.sendMessage(mmd("<red>You don't have a home location set.</red>"))
                })
                return@Runnable
            }

            plugin.playerRepository.deletePlayerHome(player.uniqueId)
            player.sendMessage(mmd("<gray>Your home location has been deleted.</gray>"))
        })
    }

    @CommandAlias("home")
    @Description("Teleport to your home location")
    @CommandPermission("pixelessentials.home")
    fun onHome(player: Player) {
        if (plugin.teleportManager.isOnCooldown(player)) {
            player.sendMessage(
                mmd(
                    "<red>You are on cooldown for another</red> <white>${
                        plugin.teleportManager.getRemainingCooldown(
                            player
                        )
                    } seconds</white><red>.</red>"
                )
            )
            return
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val home = plugin.playerRepository.getPlayerHome(player.uniqueId) ?: run {
                player.sendMessage(mmd("<red>You don't have a home location set.</red>"))
                return@Runnable
            }

            Bukkit.getScheduler().runTask(plugin, Runnable inner@{
                val world = Bukkit.getWorld(home.world) ?: run {
                    player.sendMessage(mmd("<red>World <white>${home.world}</white> does not exist.</red>"))
                    return@inner
                }

                plugin.teleportManager.scheduleTeleport(
                    TeleportRequest.ToLocation(
                        player = player,
                        locationProvider = {
                            Location(
                                world,
                                home.x,
                                home.y,
                                home.z,
                                home.yaw.toFloat(),
                                home.pitch.toFloat()
                            )
                        },
                        destinationName = "home"
                    )
                )
            })
        })
    }

    @CommandAlias("setwarp")
    @Description("Set a warp location")
    @CommandPermission("pixelessentials.setwarp")
    fun onSetwarp(player: Player, name: String) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            plugin.warpRepository.setWarp(
                Warp(
                    name = name,
                    x = player.location.x,
                    y = player.location.y,
                    z = player.location.z,
                    pitch = player.location.pitch.toDouble(),
                    yaw = player.location.yaw.toDouble(),
                    world = player.location.world.name
                )
            )

            Bukkit.getScheduler().runTask(plugin, Runnable {
                player.sendMessage(mmd("<gray>Warp location <white>$name</white> has been set.</gray>"))
            })
        })
    }

    @CommandAlias("delwarp")
    @Description("Delete a warp location")
    @CommandPermission("pixelessentials.setwarp")
    fun onDelwarp(player: Player, name: String) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            plugin.warpRepository.getWarp(name) ?: run {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    player.sendMessage(mmd("<red>Warp location <white>$name</white> does not exist.</red>"))
                })
                return@Runnable
            }

            plugin.warpRepository.deleteWarp(name)

            Bukkit.getScheduler().runTask(plugin, Runnable {
                player.sendMessage(mmd("<gray>Warp location <white>$name</white> has been deleted.</gray>"))
            })
        })
    }

    @CommandAlias("warp")
    @Description("Teleport to a warp location")
    @CommandPermission("pixelessentials.warp")
    fun onWarp(player: Player, name: String) {
        if (plugin.teleportManager.isOnCooldown(player)) {
            player.sendMessage(
                mmd(
                    "<red>You are on cooldown for another</red> <white>${
                        plugin.teleportManager.getRemainingCooldown(
                            player
                        )
                    } seconds</white><red>.</red>"
                )
            )
            return
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val warp = plugin.warpRepository.getWarp(name) ?: run {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    player.sendMessage(mmd("<red>Warp location <white>$name</white> does not exist.</red>"))
                })
                return@Runnable
            }

            Bukkit.getScheduler().runTask(plugin, Runnable inner@{
                val world = Bukkit.getWorld(warp.world) ?: run {
                    player.sendMessage(mmd("<red>World <white>${warp.world}</white> does not exist.</red>"))
                    return@inner
                }

                plugin.teleportManager.scheduleTeleport(
                    TeleportRequest.ToLocation(
                        player = player,
                        locationProvider = {
                            Location(
                                world,
                                warp.x,
                                warp.y,
                                warp.z,
                                warp.yaw.toFloat(),
                                warp.pitch.toFloat()
                            )
                        },
                        destinationName = warp.name
                    )
                )
            })
        })
    }

    @CommandAlias("warps|listwarps")
    @Description("List all warp locations")
    @CommandPermission("pixelessentials.listwarps")
    fun onListwarps(player: Player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val warps = plugin.warpRepository.getWarps()

            var warpList = "<gray>Warps:</gray> "
            warps.forEachIndexed { i, warp ->
                val hoverText = """
                <gray>x:</gray> <white>${warp.x}</white>
                <gray>y:</gray> <white>${warp.y}</white>
                <gray>z:</gray> <white>${warp.z}</white>
                <gray>world:</gray> <white>${warp.world}</white>""".trimIndent()

                warpList += "<hover:show_text:'$hoverText'><white>${warp.name}</white></hover>"
                if (i < warps.size - 1) {
                    warpList += "<gray>, </gray>"
                }
            }

            Bukkit.getScheduler().runTask(plugin, Runnable inner@{
                if (warps.isEmpty()) {
                    player.sendMessage(mmd("<red>There are no warps.</red>"))
                    return@inner
                }

                player.sendMessage(mmd(warpList))
            })
        })
    }
}