package me.kyleseven.pixelessentials.utils

import me.kyleseven.pixelessentials.PixelEssentials
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

class MotdBuilder(private val plugin: PixelEssentials) {
    private val replacements = mapOf<String, (Player, PixelEssentials) -> String>(
        "{username}" to { player, _ -> player.name },
        "{displayname}" to { player, _ -> mms(player.displayName()) },
        "{uuid}" to { player, _ -> player.uniqueId.toString() },

        "{online}" to { _, plugin -> plugin.server.onlinePlayers.size.toString() },
        "{max_players}" to { _, plugin -> plugin.server.maxPlayers.toString() },
        "{version}" to { _, plugin -> plugin.server.version },

        "{server_time_12}" to { _, _ -> getServerTime("h:mm:ss a") },
        "{server_time_24}" to { _, _ -> getServerTime("H:mm:ss") },
        "{server_date_ymd}" to { _, _ -> getServerTime("y-M-d") },
        "{server_date_dmy}" to { _, _ -> getServerTime("d-M-y") },
        "{server_date_mdy}" to { _, _ -> getServerTime("M-d-y") },
        "{world_time_12}" to { player, _ -> getWorldTime(player, "h:mm:ss a") },
        "{world_time_24}" to { player, _ -> getWorldTime(player, "H:mm:ss") }
    )

    private fun getServerTime(format: String): String {
        val dateFormat = SimpleDateFormat(format)
        dateFormat.timeZone = TimeZone.getDefault()
        return dateFormat.format(Date())
    }

    private fun getWorldTime(player: Player, format: String): String {
        val world = player.world
        val worldTime = world.time
        val hours = (worldTime / 1000 + 6) % 24
        val minutes = (worldTime % 1000) * 60 / 1000
        val seconds = 0

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hours.toInt())
        calendar.set(Calendar.MINUTE, minutes.toInt())
        calendar.set(Calendar.SECOND, seconds)

        val dateFormat = SimpleDateFormat(format)
        return dateFormat.format(calendar.time)
    }

    fun build(player: Player): Component {
        // Process placeholders
        var processedMotd = plugin.configProvider.motd
        replacements.forEach { (placeholder, replacer) ->
            processedMotd = processedMotd.replace(placeholder, replacer(player, plugin))
        }

        return mmd(processedMotd)
    }
}