package me.kyleseven.pixelessentials.database.models

data class Player(
    val lastAccountName: String,
    val uuid: String,
    val ipAddress: String,
    val firstJoin: Int,
    val lastSeen: Int,
    val totalPlaytime: Int,
    val isBanned: Boolean,
    val banReason: String?
)

data class PlayerLastLocation(
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Double,
    val yaw: Double,
    val world: String
)

data class PlayerHome(
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Double,
    val yaw: Double,
    val world: String
)