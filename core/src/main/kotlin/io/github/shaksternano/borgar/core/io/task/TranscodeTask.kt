package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.media.MediaProcessConfig

class TranscodeTask(
    format: String,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessConfig = TranscodeConfig(format)
}

private class TranscodeConfig(
    private val format: String,
) : MediaProcessConfig {

    override val outputName: String? = null

    override fun transformOutputFormat(inputFormat: String): String = format
}
