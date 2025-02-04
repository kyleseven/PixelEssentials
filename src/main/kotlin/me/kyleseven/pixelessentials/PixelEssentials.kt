package me.kyleseven.pixelessentials

import co.aikar.commands.PaperCommandManager
import me.kyleseven.pixelessentials.commands.MainCommand
import me.kyleseven.pixelessentials.commands.TeleportCommands
import me.kyleseven.pixelessentials.config.PluginConfigProvider
import me.kyleseven.pixelessentials.database.DatabaseManager
import me.kyleseven.pixelessentials.database.repositories.PlayerRepository
import me.kyleseven.pixelessentials.database.repositories.WarpRepository
import me.kyleseven.pixelessentials.utils.TeleportManager
import org.bukkit.plugin.java.JavaPlugin

open class PixelEssentials : JavaPlugin() {
    lateinit var configProvider: PluginConfigProvider
    lateinit var teleportManager: TeleportManager

    lateinit var databaseManager: DatabaseManager
    lateinit var playerInfoRepository: PlayerRepository
    lateinit var warpRepository: WarpRepository

    override fun onEnable() {
        saveDefaultConfig()

        // Late init
        configProvider = PluginConfigProvider(this)
        teleportManager = TeleportManager(this)

        // Database
        databaseManager = DatabaseManager(this)
        if (!databaseManager.connect()) {
            server.pluginManager.disablePlugin(this)
            return
        }
        playerInfoRepository = PlayerRepository(databaseManager.dsl)
        warpRepository = WarpRepository(databaseManager.dsl)

        // Events
        server.pluginManager.registerEvents(teleportManager, this)

        // Commands
        val paperCommandManager = PaperCommandManager(this)
        paperCommandManager.registerCommand(MainCommand(this))
        paperCommandManager.registerCommand(TeleportCommands(this))
    }

    override fun onDisable() {
        databaseManager.disconnect()
    }

    override fun reloadConfig() {
        super.reloadConfig()
        configProvider = PluginConfigProvider(this)
    }
}