package me.kyleseven.pixelessentials.utils

import me.kyleseven.pixelessentials.PixelEssentials
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class MotdBuilder(private val plugin: PixelEssentials) {
    private val timePlaceholderRegex = Regex("\\{(server_time|world_time)(?::([^}]+))?}")

    private val replacements = mapOf<String, (CommandSender, PixelEssentials) -> String>(
        "{username}" to { sender, _ ->
            if (sender is Player) sender.name else "CONSOLE"
        },
        "{displayname}" to { sender, _ ->
            if (sender is Player) mms(sender.displayName()) else "CONSOLE"
        },
        "{uuid}" to { sender, _ ->
            if (sender is Player) sender.uniqueId.toString() else "CONSOLE"
        },
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

    private fun getWorldTime(world: World, format: String): String {
        return try {
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
            getWorldTime(world, "h:mm a")
        }
    }

    fun build(sender: CommandSender): Component {
        var processedMotd = plugin.configProvider.motd

        replacements.forEach { (placeholder, replacer) ->
            processedMotd = processedMotd.replace(placeholder, replacer(sender, plugin))
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
                "world_time" -> {
                    val world = if (sender is Player) {
                        sender.world
                    } else {
                        plugin.server.worlds.firstOrNull()
                    }

                    if (world != null) {
                        getWorldTime(world, format)
                    } else {
                        plugin.logger.warning("No world available for world_time placeholder.")
                        ""
                    }
                }

                else -> ""
            }
        }

        return mmd(processedMotd)
    }
}