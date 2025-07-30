package com.shakster.borgar.core.data.repository

import com.shakster.borgar.core.util.Identified
import com.shakster.borgar.core.util.MessagingPlatform
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.concurrent.ConcurrentHashMap

object BanRepository : Repository() {

    private val cache: MutableSet<BannedKey> = ConcurrentHashMap.newKeySet()

    override fun table(): BanRepositoryTable = BanRepositoryTable

    suspend fun init() {
        if (connected) {
            dbQuery {
                selectAll().forEach {
                    val entityId = it[table().entityId]
                    val entityType = it[table().entityType]
                    val platform = it[table().platform]
                    cache.add(BannedKey(entityId, entityType, platform))
                }
            }
        }
    }

    suspend fun create(entityId: String, entityType: EntityType, platform: MessagingPlatform) {
        dbQuery {
            insert {
                it[table().entityId] = entityId
                it[table().entityType] = entityType
                it[table().platform] = platform
            }
        }
        cache.add(BannedKey(entityId, entityType, platform))
    }

    fun readAll(entityType: EntityType, platform: MessagingPlatform): Set<String> {
        return buildSet {
            cache.forEach {
                if (it.entityType == entityType && it.platform == platform) {
                    add(it.entityId)
                }
            }
        }
    }

    suspend fun delete(entityId: String, entityType: EntityType, platform: MessagingPlatform) {
        dbQuery {
            deleteWhere {
                (table().entityId eq entityId) and
                    (table().entityType eq entityType) and
                    (table().platform eq platform)
            }
        }
        cache.remove(BannedKey(entityId, entityType, platform))
    }

    fun exists(entityId: String, entityType: EntityType, platform: MessagingPlatform): Boolean {
        return cache.contains(BannedKey(entityId, entityType, platform))
    }

    private data class BannedKey(
        val entityId: String,
        val entityType: EntityType,
        val platform: MessagingPlatform,
    )
}

object BanRepositoryTable : Table("bans") {

    val entityId = varchar("entity_id", 50)
    val entityType = enumerationByName<EntityType>("entity_type", 20)
    val platform = enumerationByName<MessagingPlatform>("platform", 20)

    override val primaryKey: PrimaryKey = PrimaryKey(
        entityId,
        entityType,
        platform,
        name = "banned_pk",
    )
}

@Suppress("unused")
enum class EntityType(
    override val id: String,
) : Identified {
    USER("user"),
    CHANNEL("channel"),
    GUILD("guild"),
}
