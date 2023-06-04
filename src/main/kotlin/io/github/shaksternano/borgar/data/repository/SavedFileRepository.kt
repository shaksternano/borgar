package io.github.shaksternano.borgar.data.repository

import io.github.shaksternano.borgar.data.VarcharIdTable
import io.github.shaksternano.borgar.data.databaseConnection
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object SavedFileRepository {

    private object SavedFileTable : VarcharIdTable(columnName = "file_alias_url", length = 2000) {
        val fileUrl = text("file_url").uniqueIndex()
        val fileAliasUrl = id
    }

    init {
        transaction(databaseConnection()) {
            SchemaUtils.createMissingTablesAndColumns(SavedFileTable)
        }
    }

    private suspend fun <T> dbQuery(block: suspend SavedFileTable.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block(SavedFileTable) }

    suspend fun addAlias(fileUrl: String, fileAliasUrl: String): Unit = dbQuery {
        insert {
            it[this.fileUrl] = fileUrl
            it[this.fileAliasUrl] = fileAliasUrl
        }
    }

    suspend fun findUrl(fileAliasUrl: String): String? = dbQuery {
        select { SavedFileTable.fileAliasUrl eq fileAliasUrl }
            .map { it[fileUrl] }
            .singleOrNull()
    }

    suspend fun findAliasUrl(fileUrl: String): String? = dbQuery {
        select { SavedFileTable.fileUrl eq fileUrl }
            .map { it[fileAliasUrl].value }
            .singleOrNull()
    }
}
