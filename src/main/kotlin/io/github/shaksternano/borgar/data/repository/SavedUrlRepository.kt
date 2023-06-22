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

object SavedUrlRepository {

    private object SavedUrlTable : VarcharIdTable(name = "saved_url", columnName = "url", length = 2000) {
        val url = id
        val aliasUrl = varchar("alias_url", 2000).uniqueIndex()
    }

    init {
        transaction(databaseConnection()) {
            SchemaUtils.create(SavedUrlTable)
        }
    }

    private suspend fun <T> dbQuery(block: suspend SavedUrlTable.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block(SavedUrlTable) }

    suspend fun createAlias(url: String, aliasUrl: String): Unit = dbQuery {
        insert {
            it[this.url] = url
            it[this.aliasUrl] = aliasUrl
        }
    }

    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    fun createAliasFuture(url: String, aliasUrl: String): CompletableFuture<Void> = GlobalScope.future {
        createAlias(url, aliasUrl)
    }.thenAccept { }

    suspend fun readAliasUrl(url: String): String? = dbQuery {
        select { SavedUrlTable.url eq url }
            .map { it[aliasUrl] }
            .singleOrNull()
    }

    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    fun readAliasUrlFuture(url: String): CompletableFuture<Optional<String>> = GlobalScope.future {
        Optional.ofNullable(readAliasUrl(url))
    }
}
