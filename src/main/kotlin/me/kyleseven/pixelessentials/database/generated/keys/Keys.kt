/*
 * This file is generated by jOOQ.
 */
package me.kyleseven.pixelessentials.database.generated.keys


import me.kyleseven.pixelessentials.database.generated.tables.*
import me.kyleseven.pixelessentials.database.generated.tables.records.*
import org.jooq.ForeignKey
import org.jooq.UniqueKey
import org.jooq.impl.DSL
import org.jooq.impl.Internal


// -------------------------------------------------------------------------
// UNIQUE and PRIMARY KEY definitions
// -------------------------------------------------------------------------

val PLAYER_HOMES__PK_PLAYER_HOMES: UniqueKey<PlayerHomesRecord> = Internal.createUniqueKey(
    PlayerHomes.PLAYER_HOMES,
    DSL.name("pk_player_homes"),
    arrayOf(PlayerHomes.PLAYER_HOMES.PLAYER_HOME_ID),
    true
)
val PLAYER_HOMES__UK_PLAYER_HOMES_1_90449728: UniqueKey<PlayerHomesRecord> = Internal.createUniqueKey(
    PlayerHomes.PLAYER_HOMES,
    DSL.name("uk_player_homes_1_90449728"),
    arrayOf(PlayerHomes.PLAYER_HOMES.PLAYER_ID),
    true
)
val PLAYER_LAST_LOCATIONS__PK_PLAYER_LAST_LOCATIONS: UniqueKey<PlayerLastLocationsRecord> = Internal.createUniqueKey(
    PlayerLastLocations.PLAYER_LAST_LOCATIONS,
    DSL.name("pk_player_last_locations"),
    arrayOf(PlayerLastLocations.PLAYER_LAST_LOCATIONS.PLAYER_ID),
    true
)
val PLAYERS__PK_PLAYERS: UniqueKey<PlayersRecord> =
    Internal.createUniqueKey(Players.PLAYERS, DSL.name("pk_players"), arrayOf(Players.PLAYERS.PLAYER_ID), true)
val PLAYERS__UK_PLAYERS_1_102966634: UniqueKey<PlayersRecord> =
    Internal.createUniqueKey(Players.PLAYERS, DSL.name("uk_players_1_102966634"), arrayOf(Players.PLAYERS.UUID), true)
val SPAWN__PK_SPAWN: UniqueKey<SpawnRecord> =
    Internal.createUniqueKey(Spawn.SPAWN, DSL.name("pk_spawn"), arrayOf(Spawn.SPAWN.SPAWN_ID), true)
val WARPS__PK_WARPS: UniqueKey<WarpsRecord> =
    Internal.createUniqueKey(Warps.WARPS, DSL.name("pk_warps"), arrayOf(Warps.WARPS.WARP_ID), true)
val WARPS__UK_WARPS_1_50937107: UniqueKey<WarpsRecord> =
    Internal.createUniqueKey(Warps.WARPS, DSL.name("uk_warps_1_50937107"), arrayOf(Warps.WARPS.NAME), true)

// -------------------------------------------------------------------------
// FOREIGN KEY definitions
// -------------------------------------------------------------------------

val PLAYER_HOMES__FK_PLAYER_HOMES_PK_PLAYERS: ForeignKey<PlayerHomesRecord, PlayersRecord> = Internal.createForeignKey(
    PlayerHomes.PLAYER_HOMES,
    DSL.name("fk_player_homes_pk_players"),
    arrayOf(PlayerHomes.PLAYER_HOMES.PLAYER_ID),
    me.kyleseven.pixelessentials.database.generated.keys.PLAYERS__PK_PLAYERS,
    arrayOf(Players.PLAYERS.PLAYER_ID),
    true
)
val PLAYER_LAST_LOCATIONS__FK_PLAYER_LAST_LOCATIONS_PK_PLAYERS: ForeignKey<PlayerLastLocationsRecord, PlayersRecord> =
    Internal.createForeignKey(
        PlayerLastLocations.PLAYER_LAST_LOCATIONS,
        DSL.name("fk_player_last_locations_pk_players"),
        arrayOf(PlayerLastLocations.PLAYER_LAST_LOCATIONS.PLAYER_ID),
        me.kyleseven.pixelessentials.database.generated.keys.PLAYERS__PK_PLAYERS,
        arrayOf(Players.PLAYERS.PLAYER_ID),
        true
    )
