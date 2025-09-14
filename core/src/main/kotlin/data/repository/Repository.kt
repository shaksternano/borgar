package com.shakster.borgar.core.data.repository

import com.shakster.borgar.core.data.databaseConnection
import com.shakster.borgar.core.io.IO_DISPATCHER
import com.shakster.borgar.core.logger
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

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

    protected suspend fun <R> dbQuery(block: Table.() -> R): R {
        return withContext(IO_DISPATCHER) {
            transaction {
                block(table())
            }
        }
    }
}
