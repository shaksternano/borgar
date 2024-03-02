package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.core.io.DataSource

data class CommandResponse(
    val content: String = "",
    val files: List<DataSource> = emptyList(),
    val suppressEmbeds: Boolean = false,
    /**
     * Setting this in the constructor does nothing.
     */
    val deferReply: Boolean = true,
    /**
     * Setting this in the constructor does nothing.
     */
    val ephemeral: Boolean = false,
    val responseData: Any? = null,
)
