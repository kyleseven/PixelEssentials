package me.kyleseven.pixelessentials.config

sealed interface ConfigProvider {
    val teleportCooldown: Int
    val teleportDelay: Int
    val teleportRequestExpiration: Int
}
