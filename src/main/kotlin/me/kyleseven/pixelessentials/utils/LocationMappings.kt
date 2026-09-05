package me.kyleseven.pixelessentials.utils

import me.kyleseven.pixelessentials.database.models.PlayerHome
import me.kyleseven.pixelessentials.database.models.Spawn
import me.kyleseven.pixelessentials.database.models.Warp
import org.bukkit.Location
import org.bukkit.World

fun Location.toPlayerHome() = PlayerHome(
    x = x,
    y = y,
    z = z,
    pitch = pitch.toDouble(),
    yaw = yaw.toDouble(),
    world = world.name
)

fun Location.toWarp(name: String) = Warp(
    name = name,
    x = x,
    y = y,
    z = z,
    pitch = pitch.toDouble(),
    yaw = yaw.toDouble(),
    world = world.name
)

fun Location.toSpawn() = Spawn(
    x = x,
    y = y,
    z = z,
    pitch = pitch.toDouble(),
    yaw = yaw.toDouble(),
    world = world.name
)

fun PlayerHome.toLocation(world: World) =
    Location(world, x, y, z, yaw.toFloat(), pitch.toFloat())

fun Warp.toLocation(world: World) =
    Location(world, x, y, z, yaw.toFloat(), pitch.toFloat())

fun Spawn.toLocation(world: World) =
    Location(world, x, y, z, yaw.toFloat(), pitch.toFloat())
