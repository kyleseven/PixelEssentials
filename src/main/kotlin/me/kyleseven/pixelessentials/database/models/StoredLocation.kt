package me.kyleseven.pixelessentials.database.models

import org.bukkit.Bukkit
import org.bukkit.Location

data class StoredLocation(
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Double,
    val yaw: Double,
    val world: String
) {
    /**
     * Convert to a Bukkit [Location].
     *
     * @return The location, or null if [world] is not loaded.
     */
    fun toLocation(): Location? {
        val world = Bukkit.getWorld(world) ?: return null
        return Location(world, x, y, z, yaw.toFloat(), pitch.toFloat())
    }
}

fun Location.toStoredLocation(): StoredLocation {
    return StoredLocation(
        x = x,
        y = y,
        z = z,
        pitch = pitch.toDouble(),
        yaw = yaw.toDouble(),
        world = world.name
    )
}