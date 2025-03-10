/*
 * This file is generated by jOOQ.
 */
package me.kyleseven.pixelessentials.database.generated.tables.records


import me.kyleseven.pixelessentials.database.generated.tables.PlayerHomes

import org.jooq.Record1
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class PlayerHomesRecord private constructor() : UpdatableRecordImpl<PlayerHomesRecord>(PlayerHomes.PLAYER_HOMES) {

    open var playerHomeId: Long?
        set(value): Unit = set(0, value)
        get(): Long? = get(0) as Long?

    open var playerId: Long
        set(value): Unit = set(1, value)
        get(): Long = get(1) as Long

    open var x: Double
        set(value): Unit = set(2, value)
        get(): Double = get(2) as Double

    open var y: Double
        set(value): Unit = set(3, value)
        get(): Double = get(3) as Double

    open var z: Double
        set(value): Unit = set(4, value)
        get(): Double = get(4) as Double

    open var pitch: Double
        set(value): Unit = set(5, value)
        get(): Double = get(5) as Double

    open var yaw: Double
        set(value): Unit = set(6, value)
        get(): Double = get(6) as Double

    open var world: String
        set(value): Unit = set(7, value)
        get(): String = get(7) as String

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<Long?> = super.key() as Record1<Long?>

    /**
     * Create a detached, initialised PlayerHomesRecord
     */
    constructor(
        playerHomeId: Long? = null,
        playerId: Long,
        x: Double,
        y: Double,
        z: Double,
        pitch: Double,
        yaw: Double,
        world: String
    ) : this() {
        this.playerHomeId = playerHomeId
        this.playerId = playerId
        this.x = x
        this.y = y
        this.z = z
        this.pitch = pitch
        this.yaw = yaw
        this.world = world
        resetChangedOnNotNull()
    }
}
