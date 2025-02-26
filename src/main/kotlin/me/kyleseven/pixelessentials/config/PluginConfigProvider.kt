package me.kyleseven.pixelessentials.config

import me.kyleseven.pixelessentials.PixelEssentials

class PluginConfigProvider(private val plugin: PixelEssentials) : ConfigProvider {
    // AFK
    override val afkTimeout: Int
        get() = plugin.config.getInt("afk.timeout")

    // Chat
    override val customChatEnabled: Boolean
        get() = plugin.config.getBoolean("chat.custom-format.enabled")

    override val customChatFormat: String
        get() = plugin.config.getString("chat.custom-format.format").toString()

    // MOTD
    override val motdShowOnJoin: Boolean
        get() = plugin.config.getBoolean("motd.show-on-join")

    override val motd: String
        get() = plugin.config.getString("motd.message").toString()

    // Rules
    override val rules: String
        get() = plugin.config.getString("rules.message").toString()

    // Messages
    override val welcomeMessageEnabled: Boolean
        get() = plugin.config.getBoolean("messages.welcome-message.enabled")

    override val welcomeMessage: String
        get() = plugin.config.getString("messages.welcome-message.message").toString()

    override val customJoinMessageEnabled: Boolean
        get() = plugin.config.getBoolean("messages.custom-join-message.enabled")

    override val customJoinMessage: String
        get() = plugin.config.getString("messages.custom-join-message.message").toString()

    override val customLeaveMessageEnabled: Boolean
        get() = plugin.config.getBoolean("messages.custom-leave-message.enabled")

    override val customLeaveMessage: String
        get() = plugin.config.getString("messages.custom-leave-message.message").toString()

    // Teleportation
    override val teleportCooldown: Int
        get() = plugin.config.getInt("teleportation.cooldown")

    override val teleportDelay: Int
        get() = plugin.config.getInt("teleportation.delay")

    override val teleportRequestExpiration: Int
        get() = plugin.config.getInt("teleportation.request-expiration")

    override val backOnDeathNotificationEnabled: Boolean
        get() = plugin.config.getBoolean("teleportation.back-on-death-notification")
}