package me.kyleseven.pixelessentials.database.repositories

import me.kyleseven.pixelessentials.database.generated.tables.references.WARPS
import me.kyleseven.pixelessentials.database.models.Warp
import org.jooq.DSLContext

class WarpRepository(private val dsl: DSLContext) {
    fun getWarp(name: String): Warp? {
        return dsl.selectFrom(WARPS)
            .where(WARPS.NAME.eq(name))
            .fetchOne()?.let { warp ->
                Warp(
                    name = warp.name,
                    x = warp.x,
                    y = warp.y,
                    z = warp.z,
                    pitch = warp.pitch,
                    yaw = warp.yaw,
                    world = warp.world
                )
            }
    }

    fun setWarp(warp: Warp) {
        dsl.insertInto(WARPS)
            .set(WARPS.NAME, warp.name)
            .set(WARPS.X, warp.x)
            .set(WARPS.Y, warp.y)
            .set(WARPS.Z, warp.z)
            .set(WARPS.PITCH, warp.pitch)
            .set(WARPS.YAW, warp.yaw)
            .set(WARPS.WORLD, warp.world)
            .onConflict(WARPS.NAME)
            .doUpdate()
            .set(WARPS.X, warp.x)
            .set(WARPS.Y, warp.y)
            .set(WARPS.Z, warp.z)
            .set(WARPS.PITCH, warp.pitch)
            .set(WARPS.YAW, warp.yaw)
            .set(WARPS.WORLD, warp.world)
            .execute()
    }

    fun deleteWarp(name: String) {
        dsl.deleteFrom(WARPS)
            .where(WARPS.NAME.eq(name))
            .execute()
    }

    fun getWarps(): List<Warp> {
        return dsl.selectFrom(WARPS)
            .fetch { warp ->
                Warp(
                    name = warp.name,
                    x = warp.x,
                    y = warp.y,
                    z = warp.z,
                    pitch = warp.pitch,
                    yaw = warp.yaw,
                    world = warp.world
                )
            }
    }
}