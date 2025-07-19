package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.io.DataSource

data class CommandResponse(
    val content: String = "",
    val files: List<DataSource> = emptyList(),
    val suppressEmbeds: Boolean = false,
    val responseData: Any? = null,
)
