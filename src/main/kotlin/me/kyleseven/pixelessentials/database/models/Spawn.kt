package me.kyleseven.pixelessentials.database.models

data class Spawn(
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Double,
    val yaw: Double,
    val world: String
)