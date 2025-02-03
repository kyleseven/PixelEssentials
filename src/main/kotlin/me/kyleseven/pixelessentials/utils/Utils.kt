package me.kyleseven.pixelessentials.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

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