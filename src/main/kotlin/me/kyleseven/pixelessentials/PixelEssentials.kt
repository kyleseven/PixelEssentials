package me.kyleseven.pixelessentials

import co.aikar.commands.MessageType
import co.aikar.commands.PaperCommandManager
import me.kyleseven.pixelessentials.commands.*
import me.kyleseven.pixelessentials.commands.teleport.*
import me.kyleseven.pixelessentials.config.PluginConfigProvider
import me.kyleseven.pixelessentials.database.DatabaseManager
import me.kyleseven.pixelessentials.database.repositories.PlayerRepository
import me.kyleseven.pixelessentials.database.repositories.SpawnRepository
import me.kyleseven.pixelessentials.database.repositories.WarpRepository
import me.kyleseven.pixelessentials.listeners.AFKListener
import me.kyleseven.pixelessentials.listeners.ChatListener
import me.kyleseven.pixelessentials.listeners.PlayerListener
import me.kyleseven.pixelessentials.managers.AFKManager
import me.kyleseven.pixelessentials.managers.PlaytimeTracker
import me.kyleseven.pixelessentials.managers.teleport.TeleportManager
import me.kyleseven.pixelessentials.managers.teleport.TpaManager
import me.kyleseven.pixelessentials.utils.MotdBuilder
import net.milkbowl.vault.chat.Chat
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin

open class PixelEssentials : JavaPlugin() {
    lateinit var databaseManager: DatabaseManager
        private set
    lateinit var playerRepository: PlayerRepository
        private set
    lateinit var warpRepository: WarpRepository
        private set
    lateinit var spawnRepository: SpawnRepository
        private set

    lateinit var configProvider: PluginConfigProvider
        private set
    lateinit var vaultChat: Chat
        private set
    lateinit var teleportManager: TeleportManager
        private set
    lateinit var tpaManager: TpaManager
        private set
    lateinit var afkManager: AFKManager
        private set
    lateinit var playtimeTracker: PlaytimeTracker
        private set
    lateinit var motdBuilder: MotdBuilder
        private set

    override fun onEnable() {
        // Check for PaperMC
        if (!isPaper()) {
            logger.severe("This plugin requires Paper to run. Please install Paper and try again.")
            server.pluginManager.disablePlugin(this)
            return
        }

        saveDefaultConfig()

        // Database
        databaseManager = DatabaseManager(this)
        if (!databaseManager.connect()) {
            server.pluginManager.disablePlugin(this)
            return
        }
        playerRepository = PlayerRepository(databaseManager.dsl)
        warpRepository = WarpRepository(databaseManager.dsl)
        spawnRepository = SpawnRepository(databaseManager.dsl)

        // Late init
        configProvider = PluginConfigProvider(this)
        teleportManager = TeleportManager(this)
        tpaManager = TpaManager(this)
        afkManager = AFKManager(this).apply { initialize() }
        playtimeTracker = PlaytimeTracker(this).apply { initialize() }
        motdBuilder = MotdBuilder(this)
        vaultChat = setupChat().let {
            it ?: run {
                logger.severe("Vault chat not found, disabling plugin.")
                server.pluginManager.disablePlugin(this)
                return
            }
        }

        // Events
        server.pluginManager.registerEvents(PlayerListener(this), this)
        server.pluginManager.registerEvents(ChatListener(this), this)
        server.pluginManager.registerEvents(AFKListener(this), this)

        // Commands
        val paperCommandManager = PaperCommandManager(this)
        setMessageFormats(paperCommandManager)
        paperCommandManager.registerCommand(AdminCommands(this))
        paperCommandManager.registerCommand(AliasCommands())
        paperCommandManager.registerCommand(ChatCommands())
        paperCommandManager.registerCommand(MainCommand(this))
        paperCommandManager.registerCommand(UtilityCommands(this))
        paperCommandManager.registerCommand(BackCommand(this))
        paperCommandManager.registerCommand(HomeCommands(this))
        paperCommandManager.registerCommand(SpawnCommands(this))
        paperCommandManager.registerCommand(TpaCommands(this))
        paperCommandManager.registerCommand(WarpCommands(this))
        registerCompletions(paperCommandManager)
    }

    override fun onDisable() {
        if (::playtimeTracker.isInitialized) playtimeTracker.shutdown()
        if (::afkManager.isInitialized) afkManager.shutdown()
        if (::databaseManager.isInitialized) databaseManager.disconnect()
    }

    private fun isPaper(): Boolean {
        try {
            Class.forName("io.papermc.paper.PaperBootstrap")
            return true
        } catch (_: ClassNotFoundException) {
            return false
        }
    }

    /**
     * Match ACF's built-in messages (e.g. "No player matching ...") to the plugin's colors.
     * ACF's Paper manager only accepts the legacy [ChatColor], hence the suppression.
     */
    @Suppress("DEPRECATION")
    private fun setMessageFormats(commandManager: PaperCommandManager) {
        commandManager.setFormat(MessageType.ERROR, ChatColor.RED, ChatColor.WHITE, ChatColor.RED)
        commandManager.setFormat(MessageType.SYNTAX, ChatColor.GRAY, ChatColor.WHITE, ChatColor.GRAY)
        commandManager.setFormat(MessageType.INFO, ChatColor.GRAY, ChatColor.WHITE, ChatColor.GRAY)
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