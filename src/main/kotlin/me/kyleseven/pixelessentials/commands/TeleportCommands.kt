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

    private fun checkCooldownAndNotify(target: Player, sender: Player? = null): Boolean {
        if (!plugin.teleportManager.isOnCooldown(target)) return false

        val remaining = plugin.teleportManager.getRemainingCooldown(target)
        val receiver = sender ?: target

        val message = if (sender != null) {
            "<white>${mms(target.displayName())}</white> <red>is on cooldown for another</red> <white>$remaining seconds</white><red>.</red>"
        } else {
            "<red>You are on cooldown for another</red> <white>$remaining seconds</white><red>.</red>"
        }

        receiver.sendMessage(mmd(message))
        return true
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

    @CommandAlias("back")
    @Description("Teleport to your previous location")
    @CommandPermission("pixelessentials.back")
    fun onBack(player: Player) {
        if (checkCooldownAndNotify(player)) return

        val backLocation = plugin.teleportManager.getBackLocation(player)
        if (backLocation == null) {
            player.sendMessage(mmd("<red>You don't have a location to go back to.</red>"))
            return
        }

        plugin.teleportManager.scheduleTeleport(
            TeleportRequest.ToLocation(
                player = player,
                locationProvider = { backLocation },
                destinationName = "previous location"
            )
        )
    }

    @CommandAlias("tpa")
    @Description("Request to teleport to a player")
    @CommandPermission("pixelessentials.tpa")
    @CommandCompletion("@players")
    fun onTpa(player: Player, target: OnlinePlayer) {
        if (isSelfTeleport(player, target.player) || checkCooldownAndNotify(player)) return

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
        if (isSelfTeleport(player, target.player) || checkCooldownAndNotify(target.player, player)) return

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
        Bukkit.getOnlinePlayers().forEach { plyr ->
            if (plyr != player) {
                plugin.teleportManager.scheduleTeleport(
                    TeleportRequest.ToLocation(
                        player = plyr,
                        locationProvider = { player.location },
                        destinationName = mms(player.displayName())
                    ),
                    delaySeconds = 0,
                    applyCooldown = false
                )
            }
        }

        player.sendMessage(mmd("<gray>All players have been teleported to you.</gray>"))
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
        if (checkCooldownAndNotify(player)) return

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
            val warp = plugin.warpRepository.getWarp(name)
            if (warp != null) {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    player.sendMessage(mmd("<red>Warp location <white>${warp.name}</white> already exists.</red>"))
                })
                return@Runnable
            }

            plugin.warpRepository.upsertWarp(
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
    @Description("Teleport to a warp location or list all warps")
    @CommandPermission("pixelessentials.warp")
    fun onWarp(player: Player, @Optional name: String?) {
        // List all warp locations if name is not provided
        if (name.isNullOrBlank()) {
            if (player.hasPermission("pixelessentials.warp.list")) {
                player.sendMessage(mmd("<red>You don't have permission to list warps.</red>"))
                return
            }

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

            return
        }

        // Teleport to warp location if name is provided
        if (checkCooldownAndNotify(player)) return

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
}