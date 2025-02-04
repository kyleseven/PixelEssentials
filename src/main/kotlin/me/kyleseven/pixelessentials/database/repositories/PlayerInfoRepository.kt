package me.kyleseven.pixelessentials.database.repositories

import me.kyleseven.pixelessentials.database.generated.tables.records.PlayersRecord
import me.kyleseven.pixelessentials.database.generated.tables.references.PLAYERS
import me.kyleseven.pixelessentials.database.models.Player
import org.jooq.DSLContext

class PlayerInfoRepository(private val dsl: DSLContext) {
    fun findByUUID(uuid: String): Player? {
        val record: PlayersRecord? = dsl.selectFrom(PLAYERS)
            .where(PLAYERS.UUID.eq(uuid))
            .fetchOne()
        return record?.let {
            Player(
                id = it.id!!,
                lastAccountName = it.lastAccountName,
                uuid = it.uuid,
                ipAddress = it.ipAddress,
                firstJoin = it.firstJoin,
                lastSeen = it.lastSeen,
                totalPlaytime = it.totalPlaytime,
                isBanned = it.isBanned!!,
                banReason = it.banReason
            )
        }
    }
}