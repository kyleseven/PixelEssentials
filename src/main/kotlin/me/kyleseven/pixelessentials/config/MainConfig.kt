package me.kyleseven.pixelessentials.config

import me.kyleseven.pixelessentials.PixelEssentials

class MainConfig(private val plugin: PixelEssentials) {
    val teleportCooldown: Int
        get() {
            return plugin.config.getInt("teleportation.cooldown")
        }

    val teleportDelay: Int
        get() {
            return plugin.config.getInt("teleportation.delay")
        }

    val teleportRequestExpiration: Int
        get() {
            return plugin.config.getInt("teleportation.request-expiration")
        }
}