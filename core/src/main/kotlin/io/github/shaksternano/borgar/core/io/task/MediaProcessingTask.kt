package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.MediaProcessingConfig
import io.github.shaksternano.borgar.core.media.processMedia

abstract class MediaProcessingTask(
    private val maxFileSize: Long,
) : MappedFileTask() {

    protected abstract val config: MediaProcessingConfig

    final override suspend fun process(input: DataSource): DataSource =
        processMedia(input, config, maxFileSize)

    override fun then(after: FileTask): FileTask =
        if (after is MediaProcessingTask) {
            object : MediaProcessingTask(maxFileSize) {
                override val config: MediaProcessingConfig = this@MediaProcessingTask.config then after.config
            }
        } else {
            super.then(after)
        }
}
