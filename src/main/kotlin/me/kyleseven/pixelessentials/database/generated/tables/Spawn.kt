/*
 * This file is generated by jOOQ.
 */
package me.kyleseven.pixelessentials.database.generated.tables


import me.kyleseven.pixelessentials.database.generated.DefaultSchema
import me.kyleseven.pixelessentials.database.generated.keys.SPAWN__PK_SPAWN
import me.kyleseven.pixelessentials.database.generated.tables.records.SpawnRecord
import org.jooq.*
import org.jooq.impl.AutoConverter
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Spawn(
    alias: Name,
    path: Table<out Record>?,
    childPath: ForeignKey<out Record, SpawnRecord>?,
    parentPath: InverseForeignKey<out Record, SpawnRecord>?,
    aliased: Table<SpawnRecord>?,
    parameters: Array<Field<*>?>?,
    where: Condition?
) : TableImpl<SpawnRecord>(
    alias,
    DefaultSchema.DEFAULT_SCHEMA,
    path,
    childPath,
    parentPath,
    aliased,
    parameters,
    DSL.comment(""),
    TableOptions.table(),
    where,
) {
    companion object {

        /**
         * The reference instance of <code>spawn</code>
         */
        val SPAWN: Spawn = Spawn()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<SpawnRecord> = SpawnRecord::class.java

    /**
     * The column <code>spawn.spawn_id</code>.
     */
    val SPAWN_ID: TableField<SpawnRecord, Long?> = createField(
        DSL.name("spawn_id"),
        SQLDataType.BIGINT,
        this,
        "",
        AutoConverter<Long, Long>(Long::class.java, Long::class.java)
    )

    /**
     * The column <code>spawn.x</code>.
     */
    val X: TableField<SpawnRecord, Double?> = createField(DSL.name("x"), SQLDataType.DOUBLE.nullable(false), this, "")

    /**
     * The column <code>spawn.y</code>.
     */
    val Y: TableField<SpawnRecord, Double?> = createField(DSL.name("y"), SQLDataType.DOUBLE.nullable(false), this, "")

    /**
     * The column <code>spawn.z</code>.
     */
    val Z: TableField<SpawnRecord, Double?> = createField(DSL.name("z"), SQLDataType.DOUBLE.nullable(false), this, "")

    /**
     * The column <code>spawn.pitch</code>.
     */
    val PITCH: TableField<SpawnRecord, Double?> =
        createField(DSL.name("pitch"), SQLDataType.DOUBLE.nullable(false), this, "")

    /**
     * The column <code>spawn.yaw</code>.
     */
    val YAW: TableField<SpawnRecord, Double?> =
        createField(DSL.name("yaw"), SQLDataType.DOUBLE.nullable(false), this, "")

    /**
     * The column <code>spawn.world</code>.
     */
    val WORLD: TableField<SpawnRecord, String?> =
        createField(DSL.name("world"), SQLDataType.CLOB.nullable(false), this, "")

    private constructor(alias: Name, aliased: Table<SpawnRecord>?) : this(alias, null, null, null, aliased, null, null)
    private constructor(alias: Name, aliased: Table<SpawnRecord>?, parameters: Array<Field<*>?>?) : this(
        alias,
        null,
        null,
        null,
        aliased,
        parameters,
        null
    )

    private constructor(alias: Name, aliased: Table<SpawnRecord>?, where: Condition?) : this(
        alias,
        null,
        null,
        null,
        aliased,
        null,
        where
    )

    /**
     * Create an aliased <code>spawn</code> table reference
     */
    constructor(alias: String) : this(DSL.name(alias))

    /**
     * Create an aliased <code>spawn</code> table reference
     */
    constructor(alias: Name) : this(alias, null)

    /**
     * Create a <code>spawn</code> table reference
     */
    constructor() : this(DSL.name("spawn"), null)
    override fun getSchema(): Schema? = if (aliased()) null else DefaultSchema.DEFAULT_SCHEMA
    override fun getPrimaryKey(): UniqueKey<SpawnRecord> = SPAWN__PK_SPAWN
    override fun `as`(alias: String): Spawn = Spawn(DSL.name(alias), this)
    override fun `as`(alias: Name): Spawn = Spawn(alias, this)
    override fun `as`(alias: Table<*>): Spawn = Spawn(alias.qualifiedName, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Spawn = Spawn(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Spawn = Spawn(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): Spawn = Spawn(name.qualifiedName, null)

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Condition?): Spawn = Spawn(qualifiedName, if (aliased()) this else null, condition)

    /**
     * Create an inline derived table from this table
     */
    override fun where(conditions: Collection<Condition>): Spawn = where(DSL.and(conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(vararg conditions: Condition?): Spawn = where(DSL.and(*conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Field<Boolean?>?): Spawn = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL
    override fun where(condition: SQL): Spawn = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL
    override fun where(@Stringly.SQL condition: String): Spawn = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL
    override fun where(@Stringly.SQL condition: String, vararg binds: Any?): Spawn =
        where(DSL.condition(condition, *binds))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL
    override fun where(@Stringly.SQL condition: String, vararg parts: QueryPart): Spawn =
        where(DSL.condition(condition, *parts))

    /**
     * Create an inline derived table from this table
     */
    override fun whereExists(select: Select<*>): Spawn = where(DSL.exists(select))

    /**
     * Create an inline derived table from this table
     */
    override fun whereNotExists(select: Select<*>): Spawn = where(DSL.notExists(select))
}
