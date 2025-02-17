/*
 * This file is generated by jOOQ.
 */
package me.kyleseven.pixelessentials.database.generated.indexes


import me.kyleseven.pixelessentials.database.generated.tables.Players
import me.kyleseven.pixelessentials.database.generated.tables.Warps

import org.jooq.Index
import org.jooq.impl.DSL
import org.jooq.impl.Internal



// -------------------------------------------------------------------------
// INDEX definitions
// -------------------------------------------------------------------------

val IDX_PLAYERS_PLAYTIME: Index = Internal.createIndex(
    DSL.name("idx_players_playtime"),
    Players.PLAYERS,
    arrayOf(Players.PLAYERS.TOTAL_PLAYTIME),
    false
)
val IDX_PLAYERS_UUID: Index =
    Internal.createIndex(DSL.name("idx_players_uuid"), Players.PLAYERS, arrayOf(Players.PLAYERS.UUID), false)
val IDX_WARPS_NAME: Index =
    Internal.createIndex(DSL.name("idx_warps_name"), Warps.WARPS, arrayOf(Warps.WARPS.NAME), false)
