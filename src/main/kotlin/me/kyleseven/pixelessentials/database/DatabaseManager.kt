package me.kyleseven.pixelessentials.database

import me.kyleseven.pixelessentials.PixelEssentials
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

class DatabaseManager(private val plugin: PixelEssentials) {
    private lateinit var connection: Connection
    lateinit var dsl: DSLContext

    fun connect(): Boolean {
        val databaseDir = File(plugin.dataFolder, "database").apply { mkdirs() }
        val jdbcUrl = "jdbc:sqlite:${databaseDir.absolutePath}/data.db"

        try {
            connection = DriverManager.getConnection(jdbcUrl).apply {
                autoCommit = true
                createStatement().use { statement ->
                    statement.execute("PRAGMA journal_mode=WAL;")
                    statement.execute("PRAGMA foreign_keys=ON;")
                }
            }

            // Disable Flyway logging
            Configurator.setLevel("org.flywaydb.core", Level.WARN)

            // Run Flyway Migrations
            Flyway.configure(plugin.javaClass.classLoader)
                .dataSource(jdbcUrl, "", "")
                .locations("classpath:db/migration")
                .load()
                .migrate()

            dsl = DSL.using(connection, SQLDialect.SQLITE)

            // Disable jOOQ logging
            dsl.settings().apply { withExecuteLogging(false) }
            System.setProperty("org.jooq.no-logo", "true")
            System.setProperty("org.jooq.no-tips", "true")
            System.setProperty(
                "org.jooq.log.org.jooq.impl.DefaultExecuteContext.logVersionSupport", "ERROR"
            )

            return true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to connect to the database: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    fun getConnection(): Connection {
        return connection
    }

    fun disconnect() {
        try {
            if (::connection.isInitialized && !connection.isClosed) {
                connection.createStatement().use { statement ->
                    statement.execute("PRAGMA wal_checkpoint(TRUNCATE);")
                }
                connection.close()
            }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to disconnect from the database: ${e.message}")
            e.printStackTrace()
        }
    }
}