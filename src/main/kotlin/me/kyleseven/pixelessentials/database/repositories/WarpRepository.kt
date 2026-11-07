package me.kyleseven.pixelessentials.database.repositories

import me.kyleseven.pixelessentials.database.generated.tables.records.WarpsRecord
import me.kyleseven.pixelessentials.database.generated.tables.references.PLAYERS
import me.kyleseven.pixelessentials.database.generated.tables.references.WARPS
import me.kyleseven.pixelessentials.database.models.StoredLocation
import me.kyleseven.pixelessentials.database.models.Warp
import org.jooq.DSLContext
import java.util.*

class WarpRepository(private val dsl: DSLContext) {
    fun getWarp(name: String): Warp? {
        return dsl.selectFrom(WARPS)
            .where(WARPS.NAME.eq(name))
            .fetchOne()?.toWarp()
    }

    fun upsertWarp(warp: Warp, creatorUUID: UUID) {
        val location = warp.location
        dsl.insertInto(WARPS)
            .set(WARPS.NAME, warp.name)
            .set(
                WARPS.PLAYER_ID,
                dsl.select(PLAYERS.PLAYER_ID)
                    .from(PLAYERS)
                    .where(PLAYERS.UUID.eq(creatorUUID.toString()))
            )
            .set(WARPS.X, location.x)
            .set(WARPS.Y, location.y)
            .set(WARPS.Z, location.z)
            .set(WARPS.PITCH, location.pitch)
            .set(WARPS.YAW, location.yaw)
            .set(WARPS.WORLD, location.world)
            .onConflict(WARPS.NAME)
            .doUpdate()
            .set(
                WARPS.PLAYER_ID,
                dsl.select(PLAYERS.PLAYER_ID)
                    .from(PLAYERS)
                    .where(PLAYERS.UUID.eq(creatorUUID.toString()))
            )
            .set(WARPS.X, location.x)
            .set(WARPS.Y, location.y)
            .set(WARPS.Z, location.z)
            .set(WARPS.PITCH, location.pitch)
            .set(WARPS.YAW, location.yaw)
            .set(WARPS.WORLD, location.world)
            .execute()
    }

    fun deleteWarp(name: String) {
        dsl.deleteFrom(WARPS)
            .where(WARPS.NAME.eq(name))
            .execute()
    }

    fun getWarps(): List<Warp> {
        return dsl.selectFrom(WARPS)
            .fetch { it.toWarp() }
    }

    fun getWarpsPaginated(page: Int, pageSize: Int): List<Warp> {
        return dsl.selectFrom(WARPS)
            .limit(pageSize)
            .offset((page - 1) * pageSize)
            .fetch { it.toWarp() }
    }

    private fun WarpsRecord.toWarp(): Warp {
        return Warp(
            name = name,
            location = StoredLocation(
                x = x,
                y = y,
                z = z,
                pitch = pitch,
                yaw = yaw,
                world = world
            )
        )
    }
}