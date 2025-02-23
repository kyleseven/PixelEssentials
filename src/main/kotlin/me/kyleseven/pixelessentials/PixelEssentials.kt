package me.kyleseven.pixelessentials

import co.aikar.commands.PaperCommandManager
import me.kyleseven.pixelessentials.commands.*
import me.kyleseven.pixelessentials.config.PluginConfigProvider
import me.kyleseven.pixelessentials.database.DatabaseManager
import me.kyleseven.pixelessentials.database.repositories.PlayerRepository
import me.kyleseven.pixelessentials.database.repositories.SpawnRepository
import me.kyleseven.pixelessentials.database.repositories.WarpRepository
import me.kyleseven.pixelessentials.listeners.ChatListener
import me.kyleseven.pixelessentials.listeners.PlayerListener
import me.kyleseven.pixelessentials.utils.MotdBuilder
import me.kyleseven.pixelessentials.utils.TeleportManager
import net.milkbowl.vault.chat.Chat
import org.bukkit.Material
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin

open class PixelEssentials : JavaPlugin() {
    lateinit var configProvider: PluginConfigProvider
    lateinit var vaultChat: Chat
    lateinit var teleportManager: TeleportManager
    lateinit var motdBuilder: MotdBuilder

    lateinit var databaseManager: DatabaseManager
    lateinit var playerRepository: PlayerRepository
    lateinit var warpRepository: WarpRepository
    lateinit var spawnRepository: SpawnRepository

    override fun onEnable() {
        saveDefaultConfig()

        // Late init
        configProvider = PluginConfigProvider(this)
        teleportManager = TeleportManager(this)
        motdBuilder = MotdBuilder(this)
        vaultChat = setupChat().let {
            it ?: run {
                logger.severe("Vault chat not found, disabling plugin.")
                server.pluginManager.disablePlugin(this)
                return
            }
        }

        // Database
        databaseManager = DatabaseManager(this)
        if (!databaseManager.connect()) {
            server.pluginManager.disablePlugin(this)
            return
        }
        playerRepository = PlayerRepository(databaseManager.dsl)
        warpRepository = WarpRepository(databaseManager.dsl)
        spawnRepository = SpawnRepository(databaseManager.dsl)

        // Events
        server.pluginManager.registerEvents(PlayerListener(this), this)
        server.pluginManager.registerEvents(ChatListener(this), this)

        // Commands
        val paperCommandManager = PaperCommandManager(this)
        paperCommandManager.registerCommand(AdminCommands(this))
        paperCommandManager.registerCommand(AliasCommands())
        paperCommandManager.registerCommand(ChatCommands())
        paperCommandManager.registerCommand(MainCommand(this))
        paperCommandManager.registerCommand(TeleportCommands(this))
        paperCommandManager.registerCommand(UtilityCommands(this))
        registerCompletions(paperCommandManager)
    }

    override fun onDisable() {
        databaseManager.disconnect()
    }

    private fun registerCompletions(commandManager: PaperCommandManager) {
        commandManager.commandCompletions.registerCompletion("materials") {
            Material.entries.filter { it.isItem }.map { it.name.lowercase() }
        }
    }

    private fun setupChat(): Chat? {
        val rsp: RegisteredServiceProvider<Chat> =
            server.servicesManager.getRegistration(Chat::class.java) ?: return null
        return rsp.provider
    }
}