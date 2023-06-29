package io.github.shaksternano.borgar.data.repository

import io.github.shaksternano.borgar.data.databaseConnection
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

abstract class Repository<T : Table> {

    init {
        transaction(databaseConnection()) {
            SchemaUtils.create(table())
        }
    }

    protected abstract fun table(): T

    protected suspend fun <R> dbQuery(block: suspend T.() -> R): R =
        newSuspendedTransaction(Dispatchers.IO) { block(table()) }
}
