package com.shakster.borgar.messaging.builder

import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.messaging.command.CommandResponse

data class MessageCreateBuilder(
    var content: String = "",
    val files: MutableList<DataSource> = mutableListOf(),
    val referencedMessageIds: MutableList<String> = mutableListOf(),
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
