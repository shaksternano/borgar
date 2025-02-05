package io.github.shaksternano.borgar.core.data.repository

import io.github.shaksternano.borgar.core.data.databaseConnection
import io.github.shaksternano.borgar.core.logger
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

abstract class Repository {

    init {
        try {
            transaction(databaseConnection()) {
                SchemaUtils.create(table())
            }
        } catch (t: Throwable) {
            logger.error("Failed to create table", t)
        }
    }

    protected abstract fun table(): Table

    protected suspend fun <R> dbQuery(block: suspend Table.() -> R): R =
        newSuspendedTransaction(Dispatchers.IO) { block(table()) }
}
