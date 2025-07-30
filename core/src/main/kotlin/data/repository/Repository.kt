package com.shakster.borgar.core.data.repository

import com.shakster.borgar.core.data.databaseConnection
import com.shakster.borgar.core.io.IO_DISPATCHER
import com.shakster.borgar.core.logger
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

abstract class Repository {

    val connected: Boolean

    init {
        connected = try {
            transaction(databaseConnection()) {
                SchemaUtils.create(table())
            }
            true
        } catch (t: Throwable) {
            logger.error("Failed to create table", t)
            false
        }
    }

    protected abstract fun table(): Table

    protected suspend fun <R> dbQuery(block: suspend Table.() -> R): R =
        newSuspendedTransaction(IO_DISPATCHER) { block(table()) }
}
