package com.shakster.borgar.core.task

import com.shakster.borgar.core.media.MediaProcessingConfig

class TranscodeTask(
    format: String,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = TranscodeConfig(format)
}

class TranscodeConfig(
    private val format: String,
) : MediaProcessingConfig {

    override fun transformOutputFormat(inputFormat: String): String = format
}
