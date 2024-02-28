package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.media.MediaProcessingConfig

class TranscodeTask(
    format: String,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = TranscodeConfig(format)
}

private class TranscodeConfig(
    private val format: String,
) : MediaProcessingConfig {

    override val outputName: String? = null

    override fun transformOutputFormat(inputFormat: String): String = format
}
