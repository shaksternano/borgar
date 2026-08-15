package com.shakster.borgar.messaging.entity

import com.shakster.borgar.core.io.DataSource

data class FileUpload(
    val content: DataSource,
    val spoiler: Boolean = false,
)
