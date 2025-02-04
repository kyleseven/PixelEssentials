package me.kyleseven.pixelessentials.database.models

data class Player(
    val id: Int,
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
    val playerId: Int,
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Float,
    val yaw: Float,
    val world: String
)

data class PlayerHome(
    val id: Int,
    val playerId: Int,
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Float,
    val yaw: Float,
    val world: String
)