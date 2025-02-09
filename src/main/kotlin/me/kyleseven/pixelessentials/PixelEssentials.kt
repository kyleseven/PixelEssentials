package me.kyleseven.pixelessentials

import co.aikar.commands.PaperCommandManager
import me.kyleseven.pixelessentials.commands.MainCommand
import me.kyleseven.pixelessentials.commands.TeleportCommands
import me.kyleseven.pixelessentials.commands.UtilityCommands
import me.kyleseven.pixelessentials.config.PluginConfigProvider
import me.kyleseven.pixelessentials.database.DatabaseManager
import me.kyleseven.pixelessentials.database.repositories.PlayerRepository
import me.kyleseven.pixelessentials.database.repositories.WarpRepository
import me.kyleseven.pixelessentials.listeners.PlayerListener
import me.kyleseven.pixelessentials.utils.MotdBuilder
import me.kyleseven.pixelessentials.utils.TeleportManager
import org.bukkit.plugin.java.JavaPlugin

open class PixelEssentials : JavaPlugin() {
    lateinit var configProvider: PluginConfigProvider
    lateinit var teleportManager: TeleportManager
    lateinit var motdBuilder: MotdBuilder

    lateinit var databaseManager: DatabaseManager
    lateinit var playerRepository: PlayerRepository
    lateinit var warpRepository: WarpRepository

    override fun onEnable() {
        saveDefaultConfig()

        // Late init
        configProvider = PluginConfigProvider(this)
        teleportManager = TeleportManager(this)
        motdBuilder = MotdBuilder(this)

        // Database
        databaseManager = DatabaseManager(this)
        if (!databaseManager.connect()) {
            server.pluginManager.disablePlugin(this)
            return
        }
        playerRepository = PlayerRepository(databaseManager.dsl)
        warpRepository = WarpRepository(databaseManager.dsl)

        // Events
        server.pluginManager.registerEvents(PlayerListener(this), this)

        // Commands
        val paperCommandManager = PaperCommandManager(this)
        paperCommandManager.registerCommand(MainCommand(this))
        paperCommandManager.registerCommand(TeleportCommands(this))
        paperCommandManager.registerCommand(UtilityCommands(this))
    }

    override fun onDisable() {
        databaseManager.disconnect()
    }
}