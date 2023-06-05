package io.github.shaksternano.borgar.data.repository

import io.github.shaksternano.borgar.data.VarcharIdTable
import io.github.shaksternano.borgar.data.databaseConnection
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.CompletableFuture

object SavedFileRepository {

    private object SavedFileTable : VarcharIdTable(columnName = "file_alias_url", length = 2000) {
        val fileUrl = varchar("file_url", 2000).uniqueIndex()
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

    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    fun addAliasFuture(fileUrl: String, fileAliasUrl: String): CompletableFuture<Void> = GlobalScope.future {
        addAlias(fileUrl, fileAliasUrl)
    }.thenAccept { }

    suspend fun findUrl(fileAliasUrl: String): String? = dbQuery {
        select { SavedFileTable.fileAliasUrl eq fileAliasUrl }
            .map { it[fileUrl] }
            .singleOrNull()
    }

    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    fun findUrlFuture(fileAliasUrl: String): CompletableFuture<Optional<String>> = GlobalScope.future {
        Optional.ofNullable(findUrl(fileAliasUrl))
    }

    suspend fun findAliasUrl(fileUrl: String): String? = dbQuery {
        select { SavedFileTable.fileUrl eq fileUrl }
            .map { it[fileAliasUrl].value }
            .singleOrNull()
    }

    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    fun findAliasUrlFuture(fileUrl: String): CompletableFuture<Optional<String>> = GlobalScope.future {
        Optional.ofNullable(findAliasUrl(fileUrl))
    }
}
