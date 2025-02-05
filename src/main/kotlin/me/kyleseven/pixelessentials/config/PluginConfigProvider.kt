package me.kyleseven.pixelessentials.config

import me.kyleseven.pixelessentials.PixelEssentials

class PluginConfigProvider(private val plugin: PixelEssentials) : ConfigProvider {
    override val motdShowOnJoin: Boolean
        get() = plugin.config.getBoolean("motd.show-on-join")

    override val motd: String
        get() = plugin.config.getString("motd.message").toString()

    override val welcomeMessageEnabled: Boolean
        get() = plugin.config.getBoolean("welcome-message.enabled")

    override val welcomeMessage: String
        get() = plugin.config.getString("welcome-message.message").toString()

    override val teleportCooldown: Int
        get() = plugin.config.getInt("teleportation.cooldown")

    override val teleportDelay: Int
        get() = plugin.config.getInt("teleportation.delay")

    override val teleportRequestExpiration: Int
        get() = plugin.config.getInt("teleportation.request-expiration")
}