package me.kyleseven.pixelessentials.database.repositories

import me.kyleseven.pixelessentials.database.generated.tables.references.PLAYERS
import me.kyleseven.pixelessentials.database.generated.tables.references.PLAYER_HOMES
import me.kyleseven.pixelessentials.database.generated.tables.references.PLAYER_LAST_LOCATIONS
import me.kyleseven.pixelessentials.database.models.Player
import me.kyleseven.pixelessentials.database.models.PlayerHome
import me.kyleseven.pixelessentials.database.models.PlayerLastLocation
import org.jooq.DSLContext

class PlayerInfoRepository(private val dsl: DSLContext) {
    fun getPlayer(uuid: String): Player? {
        return dsl.selectFrom(PLAYERS)
            .where(PLAYERS.UUID.eq(uuid))
            .fetchOne()?.let { player ->
                Player(
                    lastAccountName = player.lastAccountName,
                    uuid = player.uuid,
                    ipAddress = player.ipAddress,
                    firstJoin = player.firstJoin,
                    lastSeen = player.lastSeen,
                    totalPlaytime = player.totalPlaytime,
                    isBanned = player.isBanned!!,
                    banReason = player.banReason
                )
            }
    }

    fun getPlayerLastLocation(uuid: String): PlayerLastLocation? {
        return dsl.select(PLAYER_LAST_LOCATIONS.asterisk()) // Select all columns
            .from(PLAYER_LAST_LOCATIONS)
            .innerJoin(PLAYERS).on(PLAYER_LAST_LOCATIONS.PLAYER_ID.eq(PLAYERS.ID))
            .where(PLAYERS.UUID.eq(uuid))
            .fetchOne()?.let { record ->
                PlayerLastLocation(
                    x = record.get(PLAYER_LAST_LOCATIONS.X)!!,
                    y = record.get(PLAYER_LAST_LOCATIONS.Y)!!,
                    z = record.get(PLAYER_LAST_LOCATIONS.Z)!!,
                    pitch = record.get(PLAYER_LAST_LOCATIONS.PITCH)!!,
                    yaw = record.get(PLAYER_LAST_LOCATIONS.YAW)!!,
                    world = record.get(PLAYER_LAST_LOCATIONS.WORLD)!!
                )
            }
    }

    fun getPlayerHome(uuid: String): PlayerHome? {
        return dsl.select(PLAYER_HOMES.asterisk())
            .from(PLAYER_HOMES)
            .innerJoin(PLAYERS).on(PLAYER_HOMES.PLAYER_ID.eq(PLAYERS.ID))
            .where(PLAYERS.UUID.eq(uuid))
            .fetchOne()?.let { record ->
                PlayerHome(
                    x = record.get(PLAYER_HOMES.X)!!,
                    y = record.get(PLAYER_HOMES.Y)!!,
                    z = record.get(PLAYER_HOMES.Z)!!,
                    pitch = record.get(PLAYER_HOMES.PITCH)!!,
                    yaw = record.get(PLAYER_HOMES.YAW)!!,
                    world = record.get(PLAYER_HOMES.WORLD)!!
                )
            }
    }
}