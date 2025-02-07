package me.kyleseven.pixelessentials.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.text.SimpleDateFormat
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