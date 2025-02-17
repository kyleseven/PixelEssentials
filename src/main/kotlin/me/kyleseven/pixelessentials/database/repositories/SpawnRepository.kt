package me.kyleseven.pixelessentials.database.repositories

import me.kyleseven.pixelessentials.database.generated.tables.references.SPAWN
import me.kyleseven.pixelessentials.database.models.Spawn
import org.jooq.DSLContext
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class SpawnRepository(private val dsl: DSLContext) {
    private var cachedSpawn: Spawn? = null
    private val lock = ReentrantReadWriteLock()

    init {
        val loadedSpawn = dsl.select(SPAWN.asterisk())
            .from(SPAWN)
            .where(SPAWN.SPAWN_ID.eq(1))
            .fetchOne()?.let { record ->
                Spawn(
                    x = record.get(SPAWN.X)!!,
                    y = record.get(SPAWN.Y)!!,
                    z = record.get(SPAWN.Z)!!,
                    pitch = record.get(SPAWN.PITCH)!!,
                    yaw = record.get(SPAWN.YAW)!!,
                    world = record.get(SPAWN.WORLD)!!
                )
            }

        lock.write { cachedSpawn = loadedSpawn }
    }

    fun upsertSpawn(spawn: Spawn) {
        dsl.insertInto(SPAWN)
            .set(SPAWN.SPAWN_ID, 1)
            .set(SPAWN.X, spawn.x)
            .set(SPAWN.Y, spawn.y)
            .set(SPAWN.Z, spawn.z)
            .set(SPAWN.PITCH, spawn.pitch)
            .set(SPAWN.YAW, spawn.yaw)
            .set(SPAWN.WORLD, spawn.world)
            .onConflict(SPAWN.SPAWN_ID)
            .doUpdate()
            .set(SPAWN.X, spawn.x)
            .set(SPAWN.Y, spawn.y)
            .set(SPAWN.Z, spawn.z)
            .set(SPAWN.PITCH, spawn.pitch)
            .set(SPAWN.YAW, spawn.yaw)
            .set(SPAWN.WORLD, spawn.world)
            .execute()

        lock.write { cachedSpawn = spawn }
    }

    fun getSpawn(): Spawn? {
        return lock.read { cachedSpawn }
    }

    fun deleteSpawn() {
        dsl.deleteFrom(SPAWN)
            .where(SPAWN.SPAWN_ID.eq(1))
            .execute()

        lock.write { cachedSpawn = null }
    }
}