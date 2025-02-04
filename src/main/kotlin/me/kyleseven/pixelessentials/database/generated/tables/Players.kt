/*
 * This file is generated by jOOQ.
 */
package me.kyleseven.pixelessentials.database.generated.tables


import me.kyleseven.pixelessentials.database.generated.DefaultSchema
import me.kyleseven.pixelessentials.database.generated.keys.PLAYERS__PK_PLAYERS
import me.kyleseven.pixelessentials.database.generated.keys.PLAYERS__UK_PLAYERS_1_102966634
import me.kyleseven.pixelessentials.database.generated.keys.PLAYER_HOMES__FK_PLAYER_HOMES_PK_PLAYERS
import me.kyleseven.pixelessentials.database.generated.keys.PLAYER_LAST_LOCATIONS__FK_PLAYER_LAST_LOCATIONS_PK_PLAYERS
import me.kyleseven.pixelessentials.database.generated.tables.PlayerHomes.PlayerHomesPath
import me.kyleseven.pixelessentials.database.generated.tables.PlayerLastLocations.PlayerLastLocationsPath
import me.kyleseven.pixelessentials.database.generated.tables.records.PlayersRecord
import org.jooq.*
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Players(
    alias: Name,
    path: Table<out Record>?,
    childPath: ForeignKey<out Record, PlayersRecord>?,
    parentPath: InverseForeignKey<out Record, PlayersRecord>?,
    aliased: Table<PlayersRecord>?,
    parameters: Array<Field<*>?>?,
    where: Condition?
) : TableImpl<PlayersRecord>(
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
         * The reference instance of <code>players</code>
         */
        val PLAYERS: Players = Players()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<PlayersRecord> = PlayersRecord::class.java

    /**
     * The column <code>players.id</code>.
     */
    val ID: TableField<PlayersRecord, Int?> = createField(DSL.name("id"), SQLDataType.INTEGER.identity(true), this, "")

    /**
     * The column <code>players.last_account_name</code>.
     */
    val LAST_ACCOUNT_NAME: TableField<PlayersRecord, String?> =
        createField(DSL.name("last_account_name"), SQLDataType.CLOB.nullable(false), this, "")

    /**
     * The column <code>players.uuid</code>.
     */
    val UUID: TableField<PlayersRecord, String?> =
        createField(DSL.name("uuid"), SQLDataType.CLOB.nullable(false), this, "")

    /**
     * The column <code>players.ip_address</code>.
     */
    val IP_ADDRESS: TableField<PlayersRecord, String?> =
        createField(DSL.name("ip_address"), SQLDataType.CLOB.nullable(false), this, "")

    /**
     * The column <code>players.first_join</code>.
     */
    val FIRST_JOIN: TableField<PlayersRecord, Int?> =
        createField(DSL.name("first_join"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column <code>players.last_seen</code>.
     */
    val LAST_SEEN: TableField<PlayersRecord, Int?> =
        createField(DSL.name("last_seen"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column <code>players.total_playtime</code>.
     */
    val TOTAL_PLAYTIME: TableField<PlayersRecord, Int?> =
        createField(DSL.name("total_playtime"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column <code>players.is_banned</code>.
     */
    val IS_BANNED: TableField<PlayersRecord, Boolean?> = createField(
        DSL.name("is_banned"),
        SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.BOOLEAN)),
        this,
        ""
    )

    /**
     * The column <code>players.ban_reason</code>.
     */
    val BAN_REASON: TableField<PlayersRecord, String?> = createField(DSL.name("ban_reason"), SQLDataType.CLOB, this, "")

    private constructor(alias: Name, aliased: Table<PlayersRecord>?) : this(
        alias,
        null,
        null,
        null,
        aliased,
        null,
        null
    )

    private constructor(alias: Name, aliased: Table<PlayersRecord>?, parameters: Array<Field<*>?>?) : this(
        alias,
        null,
        null,
        null,
        aliased,
        parameters,
        null
    )

    private constructor(alias: Name, aliased: Table<PlayersRecord>?, where: Condition?) : this(
        alias,
        null,
        null,
        null,
        aliased,
        null,
        where
    )

    /**
     * Create an aliased <code>players</code> table reference
     */
    constructor(alias: String) : this(DSL.name(alias))

    /**
     * Create an aliased <code>players</code> table reference
     */
    constructor(alias: Name) : this(alias, null)

    /**
     * Create a <code>players</code> table reference
     */
    constructor() : this(DSL.name("players"), null)

    constructor(
        path: Table<out Record>,
        childPath: ForeignKey<out Record, PlayersRecord>?,
        parentPath: InverseForeignKey<out Record, PlayersRecord>?
    ) : this(Internal.createPathAlias(path, childPath, parentPath), path, childPath, parentPath, PLAYERS, null, null)

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    open class PlayersPath : Players, Path<PlayersRecord> {
        constructor(
            path: Table<out Record>,
            childPath: ForeignKey<out Record, PlayersRecord>?,
            parentPath: InverseForeignKey<out Record, PlayersRecord>?
        ) : super(path, childPath, parentPath)

        private constructor(alias: Name, aliased: Table<PlayersRecord>) : super(alias, aliased)
        override fun `as`(alias: String): PlayersPath = PlayersPath(DSL.name(alias), this)
        override fun `as`(alias: Name): PlayersPath = PlayersPath(alias, this)
        override fun `as`(alias: Table<*>): PlayersPath = PlayersPath(alias.qualifiedName, this)
    }
    override fun getSchema(): Schema? = if (aliased()) null else DefaultSchema.DEFAULT_SCHEMA
    override fun getIdentity(): Identity<PlayersRecord, Int?> = super.getIdentity() as Identity<PlayersRecord, Int?>
    override fun getPrimaryKey(): UniqueKey<PlayersRecord> = PLAYERS__PK_PLAYERS
    override fun getUniqueKeys(): List<UniqueKey<PlayersRecord>> = listOf(PLAYERS__UK_PLAYERS_1_102966634)

    private lateinit var _playerHomes: PlayerHomesPath

    /**
     * Get the implicit to-many join path to the <code>player_homes</code> table
     */
    fun playerHomes(): PlayerHomesPath {
        if (!this::_playerHomes.isInitialized)
            _playerHomes = PlayerHomesPath(this, null, PLAYER_HOMES__FK_PLAYER_HOMES_PK_PLAYERS.inverseKey)

        return _playerHomes;
    }

    val playerHomes: PlayerHomesPath
        get(): PlayerHomesPath = playerHomes()

    private lateinit var _playerLastLocations: PlayerLastLocationsPath

    /**
     * Get the implicit to-many join path to the
     * <code>player_last_locations</code> table
     */
    fun playerLastLocations(): PlayerLastLocationsPath {
        if (!this::_playerLastLocations.isInitialized)
            _playerLastLocations = PlayerLastLocationsPath(
                this,
                null,
                PLAYER_LAST_LOCATIONS__FK_PLAYER_LAST_LOCATIONS_PK_PLAYERS.inverseKey
            )

        return _playerLastLocations;
    }

    val playerLastLocations: PlayerLastLocationsPath
        get(): PlayerLastLocationsPath = playerLastLocations()
    override fun `as`(alias: String): Players = Players(DSL.name(alias), this)
    override fun `as`(alias: Name): Players = Players(alias, this)
    override fun `as`(alias: Table<*>): Players = Players(alias.qualifiedName, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Players = Players(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Players = Players(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): Players = Players(name.qualifiedName, null)

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Condition?): Players =
        Players(qualifiedName, if (aliased()) this else null, condition)

    /**
     * Create an inline derived table from this table
     */
    override fun where(conditions: Collection<Condition>): Players = where(DSL.and(conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(vararg conditions: Condition?): Players = where(DSL.and(*conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Field<Boolean?>?): Players = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL
    override fun where(condition: SQL): Players = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL
    override fun where(@Stringly.SQL condition: String): Players = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL
    override fun where(@Stringly.SQL condition: String, vararg binds: Any?): Players =
        where(DSL.condition(condition, *binds))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL
    override fun where(@Stringly.SQL condition: String, vararg parts: QueryPart): Players =
        where(DSL.condition(condition, *parts))

    /**
     * Create an inline derived table from this table
     */
    override fun whereExists(select: Select<*>): Players = where(DSL.exists(select))

    /**
     * Create an inline derived table from this table
     */
    override fun whereNotExists(select: Select<*>): Players = where(DSL.notExists(select))
}
