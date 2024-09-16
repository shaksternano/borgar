package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.media.IdentityImageProcessor
import io.github.shaksternano.borgar.core.media.MediaProcessingConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessingConfig

class TranscodeTask(
    format: String,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = TranscodeConfig(format)
}

private class TranscodeConfig(
    private val format: String,
) : SimpleMediaProcessingConfig(
    IdentityImageProcessor,
    outputName = "",
) {

    override fun transformOutputFormat(inputFormat: String): String = format
}
