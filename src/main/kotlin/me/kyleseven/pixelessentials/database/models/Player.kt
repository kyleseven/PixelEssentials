package me.kyleseven.pixelessentials.database.models

data class Player(
    val lastAccountName: String,
    val uuid: String,
    val ipAddress: String,
    val firstJoin: Long,
    val lastSeen: Long,
    val totalPlaytime: Long
)