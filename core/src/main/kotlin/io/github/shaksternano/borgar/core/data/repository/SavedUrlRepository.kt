package io.github.shaksternano.borgar.core.data.repository

import io.github.shaksternano.borgar.core.data.VarcharIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object SavedUrlRepository : Repository() {

    override fun table(): SavedUrlTable = SavedUrlTable

    suspend fun createAlias(url: String, aliasUrl: String): Unit = dbQuery {
        insert {
            it[SavedUrlTable.url] = url
            it[SavedUrlTable.aliasUrl] = aliasUrl
        }
    }

    suspend fun readAliasUrl(url: String): String? = dbQuery {
        selectAll().where { SavedUrlTable.url eq url }
            .map { it[SavedUrlTable.aliasUrl] }
            .firstOrNull()
    }
}

object SavedUrlTable : VarcharIdTable(name = "saved_url", columnName = "url", length = 500) {
    val url = id
    val aliasUrl = varchar("alias_url", 500).uniqueIndex()
}
