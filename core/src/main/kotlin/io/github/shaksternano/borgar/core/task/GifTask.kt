package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.fileExtension
import io.github.shaksternano.borgar.core.io.filenameWithoutExtension
import io.github.shaksternano.borgar.core.media.MediaProcessingConfig
import io.github.shaksternano.borgar.core.media.isStaticOnly
import io.github.shaksternano.borgar.core.media.processMedia

class GifTask(
    private val forceTranscode: Boolean,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = GifConfig(forceTranscode)

    override suspend fun process(input: DataSource): DataSource {
        return if (forceTranscode || !isStaticOnly(input.fileExtension)) {
            val config = TranscodeConfig("gif")
            processMedia(input, config, maxFileSize)
        } else {
            input.rename(input.filenameWithoutExtension + ".gif")
        }
    }
}

private class GifConfig(
    private val forceTranscode: Boolean,
) : MediaProcessingConfig {

    override val outputExtension: String = "gif"

    override fun transformOutputFormat(inputFormat: String): String {
        return if (forceTranscode || !isStaticOnly(inputFormat)) {
            "gif"
        } else {
            inputFormat
        }
    }
}
