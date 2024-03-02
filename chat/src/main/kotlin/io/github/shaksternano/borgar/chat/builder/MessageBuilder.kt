package io.github.shaksternano.borgar.chat.builder

import io.github.shaksternano.borgar.chat.command.CommandResponse
import io.github.shaksternano.borgar.core.io.DataSource

data class MessageCreateBuilder(
    var content: String = "",
    val files: MutableList<DataSource> = mutableListOf(),
    var referencedMessageId: String? = null,
    var suppressEmbeds: Boolean = false,
    var username: String? = null,
    var avatarUrl: String? = null,
) {

    fun fromCommandResponse(response: CommandResponse) {
        content = response.content
        files.addAll(response.files)
        suppressEmbeds = response.suppressEmbeds
    }
}

data class MessageEditBuilder(
    var content: String? = null,
    val files: MutableList<DataSource>? = mutableListOf(),
)
