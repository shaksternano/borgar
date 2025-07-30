package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.io.DataSource

data class CommandResponse(
    val content: String = "",
    val files: List<DataSource> = emptyList(),
    val suppressEmbeds: Boolean = false,
    val responseData: Any? = null,
)
