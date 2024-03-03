package io.github.shaksternano.borgar.chat.interaction

import io.github.shaksternano.borgar.core.io.DataSource

data class MessageInteractionResponse(
    val content: String = "",
    val files: List<DataSource> = emptyList(),
    val suppressEmbeds: Boolean = false,
    /**
     * Setting this in the constructor does nothing.
     */
    val ephemeral: Boolean = false,
    val responseData: Any? = null,
)
