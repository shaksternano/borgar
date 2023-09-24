package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.MediaProcessConfig
import io.github.shaksternano.borgar.core.media.processMedia

class MediaProcessingTask(
    private val maxFileSize: Long,
    private val config: MediaProcessConfig,
) : MappedFileTask(true) {

    override suspend fun process(input: DataSource): DataSource {
        val fileInput = input.getOrWriteFile()
        return processMedia(fileInput, config, maxFileSize)
    }

    override fun then(after: FileTask): FileTask {
        return if (after is MediaProcessingTask) {
            MediaProcessingTask(
                maxFileSize,
                this@MediaProcessingTask.config then after.config,
            )
        } else {
            super.then(after)
        }
    }
}
