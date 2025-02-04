package me.kyleseven.pixelessentials.config

sealed interface ConfigProvider {
    val welcomeMessageEnabled: Boolean
    val welcomeMessage: String

    val teleportCooldown: Int
    val teleportDelay: Int
    val teleportRequestExpiration: Int
}
