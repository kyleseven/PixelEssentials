package me.kyleseven.pixelessentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.kyleseven.pixelessentials.PixelEssentials
import me.kyleseven.pixelessentials.database.models.PlayerHome
import me.kyleseven.pixelessentials.database.models.Spawn
import me.kyleseven.pixelessentials.database.models.Warp
import me.kyleseven.pixelessentials.utils.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
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
    @CommandPermission("pixelessentials.tpaccept")
    fun onTpaccept(player: Player) {
        if (!plugin.teleportManager.acceptRequest(player)) {
            player.sendMessage(
                mmd("<gray>You have no pending teleport requests.</gray>")
            )
        }
    }

    @CommandAlias("tpdeny")
    @Description("Deny a teleport request")
    @CommandPermission("pixelessentials.tpdeny")
    fun onTpdeny(player: Player) {
        if (!plugin.teleportManager.denyRequest(player)) {
            player.sendMessage(
                mmd("<gray>You have no pending teleport requests.</gray>")
            )
        }
    }

    @CommandAlias("tpacancel")
    @Description("Cancel a teleport request")
    @CommandPermission("pixelessentials.tpacancel")
    fun onTpacancel(player: Player) {
        plugin.teleportManager.cancelRequest(player)
    }

    @CommandAlias("sethome")
    @Description("Set your home location")
    @CommandPermission("pixelessentials.sethome")
    fun onSethome(player: Player) {
        runTaskAsync(plugin) {
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

            runTask(plugin) {
                player.sendMessage(mmd("<gray>Your home location has been set.</gray>"))
            }
        }
    }

    @CommandAlias("delhome")
    @Description("Delete your home location")
    @CommandPermission("pixelessentials.delhome")
    fun onDelhome(player: Player) {
        runTaskAsync(plugin) {
            plugin.playerRepository.getPlayerHome(player.uniqueId) ?: run {
                runTask(plugin) {
                    player.sendMessage(mmd("<red>You don't have a home location set.</red>"))
                }
                return@runTaskAsync
            }

            plugin.playerRepository.deletePlayerHome(player.uniqueId)
            player.sendMessage(mmd("<gray>Your home location has been deleted.</gray>"))
        }
    }

    @CommandAlias("home")
    @Description("Teleport to your home location")
    @CommandPermission("pixelessentials.home")
    fun onHome(player: Player) {
        if (checkCooldownAndNotify(player)) return

        runTaskAsync(plugin) {
            val home = plugin.playerRepository.getPlayerHome(player.uniqueId) ?: run {
                player.sendMessage(mmd("<red>You don't have a home location set.</red>"))
                return@runTaskAsync
            }

            runTask(plugin) {
                val world = Bukkit.getWorld(home.world) ?: run {
                    player.sendMessage(mmd("<red>World <white>${home.world}</white> does not exist.</red>"))
                    return@runTask
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
            }
        }
    }

    @CommandAlias("setwarp")
    @Description("Set a warp location")
    @CommandPermission("pixelessentials.setwarp")
    fun onSetwarp(player: Player, @Single name: String) {
        if (name.length > 32) {
            player.sendMessage(mmd("<red>Warp location name must be 32 characters or less.</red>"))
            return
        }

        runTaskAsync(plugin) {
            val warp = plugin.warpRepository.getWarp(name)
            if (warp != null) {
                runTask(plugin) {
                    player.sendMessage(mmd("<red>Warp location <white>${warp.name}</white> already exists.</red>"))
                }
                return@runTaskAsync
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

            runTask(plugin) {
                player.sendMessage(mmd("<gray>Warp location <white>$name</white> has been set.</gray>"))
            }
        }
    }

    @CommandAlias("delwarp")
    @Description("Delete a warp location")
    @CommandPermission("pixelessentials.delwarp")
    fun onDelwarp(player: Player, @Single name: String) {
        runTaskAsync(plugin) {
            plugin.warpRepository.getWarp(name) ?: run {
                runTask(plugin) {
                    player.sendMessage(mmd("<red>Warp location <white>$name</white> does not exist.</red>"))
                }
                return@runTaskAsync
            }

            plugin.warpRepository.deleteWarp(name)

            runTask(plugin) {
                player.sendMessage(mmd("<gray>Warp location <white>$name</white> has been deleted.</gray>"))
            }
        }
    }

    @CommandAlias("warp")
    @Description("Teleport to a warp location or list all warps")
    @CommandPermission("pixelessentials.warp")
    fun onWarp(sender: CommandSender, @Optional @Single name: String?) {
        // List all warp locations if name is not provided or sender is not a player
        if (name.isNullOrBlank() || sender !is Player) {
            if (!sender.hasPermission("pixelessentials.warp.list")) {
                sender.sendMessage(mmd("<red>You don't have permission to list warps.</red>"))
                return
            }

            runTaskAsync(plugin) {
                val warps = plugin.warpRepository.getWarps()

                var warpList = "<gray>Warps (${warps.size}):</gray> "
                warps.forEachIndexed { i, warp ->
                    val warpCommand = "/warp ${warp.name}"

                    val hoverText = """
                <white>$warpCommand</white>
                <gray>x:</gray> <white>${warp.x}</white>
                <gray>y:</gray> <white>${warp.y}</white>
                <gray>z:</gray> <white>${warp.z}</white>
                <gray>world:</gray> <white>${warp.world}</white>""".trimIndent()

                    warpList += "<click:run_command:'$warpCommand'><hover:show_text:'$hoverText'><white>${warp.name}</white></hover></click>"
                    if (i < warps.size - 1) {
                        warpList += "<gray>, </gray>"
                    }
                }

                runTask(plugin) {
                    if (warps.isEmpty()) {
                        sender.sendMessage(mmd("<red>There are no warps.</red>"))
                        return@runTask
                    }

                    sender.sendMessage(mmd(warpList))
                }
            }

            return
        }

        // Teleport to warp location if name is provided
        if (checkCooldownAndNotify(sender)) return

        runTaskAsync(plugin) {
            val warp = plugin.warpRepository.getWarp(name) ?: run {
                runTask(plugin) {
                    sender.sendMessage(mmd("<red>Warp location <white>$name</white> does not exist.</red>"))
                }
                return@runTaskAsync
            }

            runTask(plugin) {
                val world = Bukkit.getWorld(warp.world) ?: run {
                    sender.sendMessage(mmd("<red>World <white>${warp.world}</white> does not exist.</red>"))
                    return@runTask
                }

                plugin.teleportManager.scheduleTeleport(
                    TeleportRequest.ToLocation(
                        player = sender,
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
            }
        }
    }

    @CommandAlias("spawn")
    @Description("Teleport to spawn")
    @CommandPermission("pixelessentials.spawn")
    fun onSpawn(player: Player) {
        if (checkCooldownAndNotify(player)) return

        runTaskAsync(plugin) {
            val spawn = plugin.spawnRepository.getSpawn() ?: run {
                runTask(plugin) {
                    player.sendMessage(mmd("<red>Spawn location has not been set.</red>"))
                }
                return@runTaskAsync
            }

            runTask(plugin) {
                val world = Bukkit.getWorld(spawn.world) ?: run {
                    player.sendMessage(mmd("<red>World <white>${spawn.world}</white> does not exist.</red>"))
                    return@runTask
                }

                plugin.teleportManager.scheduleTeleport(
                    TeleportRequest.ToLocation(
                        player = player,
                        locationProvider = {
                            Location(
                                world,
                                spawn.x,
                                spawn.y,
                                spawn.z,
                                spawn.yaw.toFloat(),
                                spawn.pitch.toFloat()
                            )
                        },
                        destinationName = "spawn"
                    )
                )
            }
        }
    }

    @CommandAlias("setspawn")
    @Description("Set the spawn location")
    @CommandPermission("pixelessentials.setspawn")
    fun onSetspawn(player: Player) {
        runTaskAsync(plugin) {
            plugin.spawnRepository.upsertSpawn(
                Spawn(
                    x = player.location.x,
                    y = player.location.y,
                    z = player.location.z,
                    pitch = player.location.pitch.toDouble(),
                    yaw = player.location.yaw.toDouble(),
                    world = player.location.world.name
                )
            )

            runTask(plugin) {
                player.sendMessage(mmd("<gray>Spawn location has been set.</gray>"))
            }
        }
    }

    @CommandAlias("delspawn")
    @Description("Delete the spawn location")
    @CommandPermission("pixelessentials.delspawn")
    fun onDelspawn(player: Player) {
        runTaskAsync(plugin) {
            plugin.spawnRepository.getSpawn() ?: run {
                runTask(plugin) {
                    player.sendMessage(mmd("<red>Spawn location has not been set.</red>"))
                }
                return@runTaskAsync
            }

            plugin.spawnRepository.deleteSpawn()

            runTask(plugin) {
                player.sendMessage(mmd("<gray>Spawn location has been deleted.</gray>"))
            }
        }
    }
}