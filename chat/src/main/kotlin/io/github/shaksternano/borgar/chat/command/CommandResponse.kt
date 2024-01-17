package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.core.io.DataSource

data class CommandResponse(
    val content: String = "",
    val files: List<DataSource> = emptyList(),
    val suppressEmbeds: Boolean = false,
    val ephemeral: Boolean = false,
    val deferReply: Boolean = true,
)
