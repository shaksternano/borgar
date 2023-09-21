package io.github.shaksternano.borgar.chat.builder

import io.github.shaksternano.borgar.core.io.DataSource

data class MessageCreateBuilder(
    var content: String = "",
    val files: MutableList<DataSource> = mutableListOf(),
    var referencedMessageId: String? = null,
)
