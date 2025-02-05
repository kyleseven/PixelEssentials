package me.kyleseven.pixelessentials.config

sealed interface ConfigProvider {
    val motdShowOnJoin: Boolean
    val motd: String

    val welcomeMessageEnabled: Boolean
    val welcomeMessage: String

    val teleportCooldown: Int
    val teleportDelay: Int
    val teleportRequestExpiration: Int
}
