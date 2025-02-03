package me.kyleseven.pixelessentials

import co.aikar.commands.PaperCommandManager
import me.kyleseven.pixelessentials.commands.MainCommand
import me.kyleseven.pixelessentials.commands.TeleportCommands
import me.kyleseven.pixelessentials.config.MainConfig
import me.kyleseven.pixelessentials.utils.TeleportManager
import org.bukkit.plugin.java.JavaPlugin

open class PixelEssentials : JavaPlugin() {
    lateinit var mainConfig: MainConfig
    lateinit var teleportManager: TeleportManager

    override fun onEnable() {
        saveDefaultConfig()

        // Late init
        mainConfig = MainConfig(this)
        teleportManager = TeleportManager(this)

        // Events
        server.pluginManager.registerEvents(teleportManager, this)

        // Commands
        val paperCommandManager = PaperCommandManager(this)
        paperCommandManager.registerCommand(MainCommand(this))
        paperCommandManager.registerCommand(TeleportCommands(this))
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}