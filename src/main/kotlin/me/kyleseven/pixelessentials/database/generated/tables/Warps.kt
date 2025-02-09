/*
 * This file is generated by jOOQ.
 */
package me.kyleseven.pixelessentials.database.generated.tables


import me.kyleseven.pixelessentials.database.generated.DefaultSchema
import me.kyleseven.pixelessentials.database.generated.keys.WARPS__PK_WARPS
import me.kyleseven.pixelessentials.database.generated.keys.WARPS__UK_WARPS_1_50937107
import me.kyleseven.pixelessentials.database.generated.tables.records.WarpsRecord
import org.jooq.*
import org.jooq.impl.AutoConverter
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Warps(
    alias: Name,
    path: Table<out Record>?,
    childPath: ForeignKey<out Record, WarpsRecord>?,
    parentPath: InverseForeignKey<out Record, WarpsRecord>?,
    aliased: Table<WarpsRecord>?,
    parameters: Array<Field<*>?>?,
    where: Condition?
) : TableImpl<WarpsRecord>(
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
         * The reference instance of <code>warps</code>
         */
        val WARPS: Warps = Warps()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<WarpsRecord> = WarpsRecord::class.java

    /**
     * The column <code>warps.warp_id</code>.
     */
    val WARP_ID: TableField<WarpsRecord, Long?> = createField(
        DSL.name("warp_id"),
        SQLDataType.BIGINT.identity(true),
        this,
        "",
        AutoConverter<Long, Long>(Long::class.java, Long::class.java)
    )

    /**
     * The column <code>warps.name</code>.
     */
    val NAME: TableField<WarpsRecord, String?> =
        createField(DSL.name("name"), SQLDataType.CLOB.nullable(false), this, "")

    /**
     * The column <code>warps.x</code>.
     */
    val X: TableField<WarpsRecord, Double?> = createField(DSL.name("x"), SQLDataType.DOUBLE.nullable(false), this, "")

    /**
     * The column <code>warps.y</code>.
     */
    val Y: TableField<WarpsRecord, Double?> = createField(DSL.name("y"), SQLDataType.DOUBLE.nullable(false), this, "")

    /**
     * The column <code>warps.z</code>.
     */
    val Z: TableField<WarpsRecord, Double?> = createField(DSL.name("z"), SQLDataType.DOUBLE.nullable(false), this, "")

    /**
     * The column <code>warps.pitch</code>.
     */
    val PITCH: TableField<WarpsRecord, Double?> =
        createField(DSL.name("pitch"), SQLDataType.DOUBLE.nullable(false), this, "")

    /**
     * The column <code>warps.yaw</code>.
     */
    val YAW: TableField<WarpsRecord, Double?> =
        createField(DSL.name("yaw"), SQLDataType.DOUBLE.nullable(false), this, "")

    /**
     * The column <code>warps.world</code>.
     */
    val WORLD: TableField<WarpsRecord, String?> =
        createField(DSL.name("world"), SQLDataType.CLOB.nullable(false), this, "")

    private constructor(alias: Name, aliased: Table<WarpsRecord>?) : this(alias, null, null, null, aliased, null, null)
    private constructor(alias: Name, aliased: Table<WarpsRecord>?, parameters: Array<Field<*>?>?) : this(
        alias,
        null,
        null,
        null,
        aliased,
        parameters,
        null
    )

    private constructor(alias: Name, aliased: Table<WarpsRecord>?, where: Condition?) : this(
        alias,
        null,
        null,
        null,
        aliased,
        null,
        where
    )

    /**
     * Create an aliased <code>warps</code> table reference
     */
    constructor(alias: String) : this(DSL.name(alias))

    /**
     * Create an aliased <code>warps</code> table reference
     */
    constructor(alias: Name) : this(alias, null)

    /**
     * Create a <code>warps</code> table reference
     */
    constructor() : this(DSL.name("warps"), null)
    override fun getSchema(): Schema? = if (aliased()) null else DefaultSchema.DEFAULT_SCHEMA
    override fun getIdentity(): Identity<WarpsRecord, Long?> = super.getIdentity() as Identity<WarpsRecord, Long?>
    override fun getPrimaryKey(): UniqueKey<WarpsRecord> = WARPS__PK_WARPS
    override fun getUniqueKeys(): List<UniqueKey<WarpsRecord>> = listOf(WARPS__UK_WARPS_1_50937107)
    override fun `as`(alias: String): Warps = Warps(DSL.name(alias), this)
    override fun `as`(alias: Name): Warps = Warps(alias, this)
    override fun `as`(alias: Table<*>): Warps = Warps(alias.qualifiedName, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Warps = Warps(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Warps = Warps(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): Warps = Warps(name.qualifiedName, null)

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Condition?): Warps = Warps(qualifiedName, if (aliased()) this else null, condition)

    /**
     * Create an inline derived table from this table
     */
    override fun where(conditions: Collection<Condition>): Warps = where(DSL.and(conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(vararg conditions: Condition?): Warps = where(DSL.and(*conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Field<Boolean?>?): Warps = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL
    override fun where(condition: SQL): Warps = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL
    override fun where(@Stringly.SQL condition: String): Warps = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL
    override fun where(@Stringly.SQL condition: String, vararg binds: Any?): Warps =
        where(DSL.condition(condition, *binds))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL
    override fun where(@Stringly.SQL condition: String, vararg parts: QueryPart): Warps =
        where(DSL.condition(condition, *parts))

    /**
     * Create an inline derived table from this table
     */
    override fun whereExists(select: Select<*>): Warps = where(DSL.exists(select))

    /**
     * Create an inline derived table from this table
     */
    override fun whereNotExists(select: Select<*>): Warps = where(DSL.notExists(select))
}
