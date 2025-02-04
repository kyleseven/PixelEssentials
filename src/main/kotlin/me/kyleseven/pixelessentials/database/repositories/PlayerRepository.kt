package me.kyleseven.pixelessentials.database.repositories

import me.kyleseven.pixelessentials.database.generated.tables.references.PLAYERS
import me.kyleseven.pixelessentials.database.generated.tables.references.PLAYER_HOMES
import me.kyleseven.pixelessentials.database.generated.tables.references.PLAYER_LAST_LOCATIONS
import me.kyleseven.pixelessentials.database.models.Player
import me.kyleseven.pixelessentials.database.models.PlayerHome
import me.kyleseven.pixelessentials.database.models.PlayerLastLocation
import org.jooq.DSLContext
import java.util.*

class PlayerRepository(private val dsl: DSLContext) {
    fun getPlayer(uuid: UUID): Player? {
        return dsl.selectFrom(PLAYERS)
            .where(PLAYERS.UUID.eq(uuid.toString()))
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

    fun upsertPlayer(player: Player) {
        dsl.insertInto(PLAYERS)
            .set(PLAYERS.UUID, player.uuid)
            .set(PLAYERS.LAST_ACCOUNT_NAME, player.lastAccountName)
            .set(PLAYERS.IP_ADDRESS, player.ipAddress)
            .set(PLAYERS.FIRST_JOIN, player.firstJoin)
            .set(PLAYERS.LAST_SEEN, player.lastSeen)
            .set(PLAYERS.TOTAL_PLAYTIME, player.totalPlaytime)
            .set(PLAYERS.IS_BANNED, player.isBanned)
            .set(PLAYERS.BAN_REASON, player.banReason)
            .onConflict(PLAYERS.UUID)
            .doUpdate()
            .set(PLAYERS.LAST_ACCOUNT_NAME, player.lastAccountName)
            .set(PLAYERS.IP_ADDRESS, player.ipAddress)
            .set(PLAYERS.FIRST_JOIN, player.firstJoin)
            .set(PLAYERS.LAST_SEEN, player.lastSeen)
            .set(PLAYERS.TOTAL_PLAYTIME, player.totalPlaytime)
            .set(PLAYERS.IS_BANNED, player.isBanned)
            .set(PLAYERS.BAN_REASON, player.banReason)
            .execute()
    }

    fun updateLastSeenAndPlaytime(uuid: UUID, playtime: Int) {
        dsl.update(PLAYERS)
            .set(PLAYERS.LAST_SEEN, (System.currentTimeMillis() / 1000).toInt())
            .set(PLAYERS.TOTAL_PLAYTIME, PLAYERS.TOTAL_PLAYTIME.plus(playtime))
            .where(PLAYERS.UUID.eq(uuid.toString()))
            .execute()
    }

    fun updateBanStatus(uuid: UUID, isBanned: Boolean, banReason: String?) {
        dsl.update(PLAYERS)
            .set(PLAYERS.IS_BANNED, isBanned)
            .set(PLAYERS.BAN_REASON, banReason)
            .where(PLAYERS.UUID.eq(uuid.toString()))
            .execute()
    }

    fun getPlayerLastLocation(uuid: UUID): PlayerLastLocation? {
        return dsl.select(PLAYER_LAST_LOCATIONS.asterisk()) // Select all columns
            .from(PLAYER_LAST_LOCATIONS)
            .innerJoin(PLAYERS).on(PLAYER_LAST_LOCATIONS.PLAYER_ID.eq(PLAYERS.ID))
            .where(PLAYERS.UUID.eq(uuid.toString()))
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

    fun upsertPlayerLastLocation(uuid: UUID, location: PlayerLastLocation) {
        dsl.insertInto(PLAYER_LAST_LOCATIONS)
            .set(
                PLAYER_HOMES.PLAYER_ID,
                dsl.select(PLAYERS.ID)
                    .from(PLAYERS)
                    .where(PLAYERS.UUID.eq(uuid.toString()))
            )
            .set(PLAYER_LAST_LOCATIONS.X, location.x)
            .set(PLAYER_LAST_LOCATIONS.Y, location.y)
            .set(PLAYER_LAST_LOCATIONS.Z, location.z)
            .set(PLAYER_LAST_LOCATIONS.PITCH, location.pitch)
            .set(PLAYER_LAST_LOCATIONS.YAW, location.yaw)
            .set(PLAYER_LAST_LOCATIONS.WORLD, location.world)
            .onConflict(PLAYER_LAST_LOCATIONS.PLAYER_ID)
            .doUpdate()
            .set(PLAYER_LAST_LOCATIONS.X, location.x)
            .set(PLAYER_LAST_LOCATIONS.Y, location.y)
            .set(PLAYER_LAST_LOCATIONS.Z, location.z)
            .set(PLAYER_LAST_LOCATIONS.PITCH, location.pitch)
            .set(PLAYER_LAST_LOCATIONS.YAW, location.yaw)
            .set(PLAYER_LAST_LOCATIONS.WORLD, location.world)
            .execute()
    }

    fun getPlayerHome(uuid: UUID): PlayerHome? {
        return dsl.select(PLAYER_HOMES.asterisk())
            .from(PLAYER_HOMES)
            .innerJoin(PLAYERS).on(PLAYER_HOMES.PLAYER_ID.eq(PLAYERS.ID))
            .where(PLAYERS.UUID.eq(uuid.toString()))
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

    fun upsertPlayerHome(uuid: UUID, home: PlayerHome) {
        dsl.insertInto(PLAYER_HOMES)
            .set(
                PLAYER_HOMES.PLAYER_ID,
                dsl.select(PLAYERS.ID)
                    .from(PLAYERS)
                    .where(PLAYERS.UUID.eq(uuid.toString()))
            )
            .set(PLAYER_HOMES.X, home.x)
            .set(PLAYER_HOMES.Y, home.y)
            .set(PLAYER_HOMES.Z, home.z)
            .set(PLAYER_HOMES.PITCH, home.pitch)
            .set(PLAYER_HOMES.YAW, home.yaw)
            .set(PLAYER_HOMES.WORLD, home.world)
            .onConflict(PLAYER_HOMES.PLAYER_ID)
            .doUpdate()
            .set(PLAYER_HOMES.X, home.x)
            .set(PLAYER_HOMES.Y, home.y)
            .set(PLAYER_HOMES.Z, home.z)
            .set(PLAYER_HOMES.PITCH, home.pitch)
            .set(PLAYER_HOMES.YAW, home.yaw)
            .set(PLAYER_HOMES.WORLD, home.world)
            .execute()
    }
}