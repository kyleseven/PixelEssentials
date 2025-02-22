package me.kyleseven.pixelessentials.utils

import me.kyleseven.pixelessentials.PixelEssentials
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

/**
 * Deserialize a string into a [Component] using MiniMessage.
 *
 * @param s The string to deserialize.
 * @return The deserialized [Component].
 */
fun mmd(s: String): Component {
    return MiniMessage.miniMessage().deserialize(s)
}

/**
 * Serialize a [Component] into a string using MiniMessage.
 *
 * @param c The [Component] to serialize.
 * @return The serialized string.
 */
fun mms(c: Component): String {
    return MiniMessage.miniMessage().serialize(c)
}

/**
 * Format a timestamp into a human-readable date string.
 *
 * @param format The format to use.
 * @param timestamp The timestamp to format.
 * @return The formatted date string.
 * @throws IllegalArgumentException If the format is invalid.
 */
fun formatDate(format: String, timestamp: Long): String {
    val dateFormat = SimpleDateFormat(format)
    dateFormat.timeZone = TimeZone.getDefault()
    return dateFormat.format(Date(timestamp))
}

/**
 * Format a duration into a human-readable string.
 *
 * @param milliseconds The duration in milliseconds.
 * @return The formatted duration string.
 */
fun formatDuration(milliseconds: Long): String {
    val duration = Duration.ofMillis(milliseconds)
    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val minutes = duration.toMinutes() % 60
    val secs = duration.seconds % 60

    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m ")
        append("${secs}s")
    }.trim()
}

/**
 * Run a task asynchronously.
 *
 * @param plugin The plugin instance.
 * @param task The task to run.
 */
fun runTaskAsync(plugin: PixelEssentials, task: () -> Unit) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable(task))
}

/**
 * Run a task synchronously.
 *
 * @param plugin The plugin instance.
 * @param task The task to run.
 */
fun runTask(plugin: PixelEssentials, task: () -> Unit) {
    Bukkit.getScheduler().runTask(plugin, Runnable(task))
}