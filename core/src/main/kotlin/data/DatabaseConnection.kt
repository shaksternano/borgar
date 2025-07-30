package com.shakster.borgar.core.data

import org.jetbrains.exposed.sql.Database

private lateinit var connection: Database

fun connectToDatabase(url: String, user: String, password: String) {
    if (!::connection.isInitialized) {
        connection = Database.connect(
            url = url,
            user = user,
            password = password,
        )
    }
}

fun databaseConnection(): Database =
    if (::connection.isInitialized) {
        connection
    } else {
        throw IllegalStateException("Database connection not initialized")
    }
