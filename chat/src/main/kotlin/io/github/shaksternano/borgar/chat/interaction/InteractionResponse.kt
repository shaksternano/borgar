package io.github.shaksternano.borgar.chat.interaction

import io.github.shaksternano.borgar.core.io.DataSource

data class InteractionResponse(
    val content: String = "",
    val files: List<DataSource> = emptyList(),
    val suppressEmbeds: Boolean = false,
    val responseData: Any? = null,
)
