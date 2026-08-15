package com.shakster.borgar.messaging.command

import com.shakster.borgar.messaging.entity.FileUpload

data class CommandResponse(
    val content: String = "",
    val files: List<FileUpload> = emptyList(),
    val suppressEmbeds: Boolean = false,
    val responseData: Any? = null,
)
