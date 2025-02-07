package me.kyleseven.pixelessentials.utils

import me.kyleseven.pixelessentials.PixelEssentials
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.*

class MotdBuilder(private val plugin: PixelEssentials) {
    private val timePlaceholderRegex = Regex("\\{(server_time|world_time)(?::([^}]+))?}")

    private val replacements = mapOf<String, (Player, PixelEssentials) -> String>(
        "{username}" to { player, _ -> player.name },
        "{displayname}" to { player, _ -> mms(player.displayName()) },
        "{uuid}" to { player, _ -> player.uniqueId.toString() },
        "{online}" to { _, plugin -> plugin.server.onlinePlayers.size.toString() },
        "{max_players}" to { _, plugin -> plugin.server.maxPlayers.toString() },
        "{version}" to { _, plugin -> plugin.server.version }
    )

    private fun getServerTime(format: String): String {
        return try {
            formatDate(format, System.currentTimeMillis())
        } catch (e: IllegalArgumentException) {
            plugin.logger.warning("Invalid server_time format: '$format'. Using default format.")
            getServerTime("h:mm a")
        }
    }

    private fun getWorldTime(player: Player, format: String): String {
        return try {
            val world = player.world
            val worldTime = world.time
            val hours = (worldTime / 1000 + 6) % 24
            val minutes = (worldTime % 1000) * 60 / 1000
            val seconds = 0

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hours.toInt())
            calendar.set(Calendar.MINUTE, minutes.toInt())
            calendar.set(Calendar.SECOND, seconds)

            formatDate(format, calendar.timeInMillis)
        } catch (e: IllegalArgumentException) {
            plugin.logger.warning("Invalid world_time format: '$format'. Using default format.")
            getWorldTime(player, "h:mm a")
        }
    }

    fun build(player: Player): Component {
        var processedMotd = plugin.configProvider.motd

        replacements.forEach { (placeholder, replacer) ->
            processedMotd = processedMotd.replace(placeholder, replacer(player, plugin))
        }

        processedMotd = timePlaceholderRegex.replace(processedMotd) { matchResult ->
            val type = matchResult.groups[1]?.value ?: return@replace ""
            val format = matchResult.groups[2]?.value ?: when (type) {
                "server_time" -> "h:mm a"
                "world_time" -> "h:mm a"
                else -> ""
            }

            when (type) {
                "server_time" -> getServerTime(format)
                "world_time" -> getWorldTime(player, format)
                else -> ""
            }
        }

        return mmd(processedMotd)
    }
}