package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.MediaProcessConfig
import io.github.shaksternano.borgar.core.media.processMedia

abstract class MediaProcessingTask(
    private val maxFileSize: Long,
) : MappedFileTask(
    requireInput = true,
) {

    protected abstract val config: MediaProcessConfig

    final override suspend fun process(input: DataSource): DataSource =
        processMedia(input, config, maxFileSize)

    override fun then(after: FileTask): FileTask {
        return if (after is MediaProcessingTask) {
            object : MediaProcessingTask(maxFileSize) {
                override val config: MediaProcessConfig = this@MediaProcessingTask.config then after.config
            }
        } else {
            super.then(after)
        }
    }
}
