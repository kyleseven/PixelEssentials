package me.kyleseven.pixelessentials.config

sealed interface ConfigProvider {
    val afkTimeout: Int

    val customChatEnabled: Boolean
    val customChatFormat: String

    val motdShowOnJoin: Boolean
    val motd: String

    val rules: String

    val welcomeMessageEnabled: Boolean
    val welcomeMessage: String

    val customJoinMessageEnabled: Boolean
    val customJoinMessage: String

    val customLeaveMessageEnabled: Boolean
    val customLeaveMessage: String

    val teleportCooldown: Int
    val teleportDelay: Int
    val teleportRequestExpiration: Int
    val backOnDeathNotificationEnabled: Boolean
}
