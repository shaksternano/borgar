package com.shakster.borgar.core.task

import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.media.MediaProcessingConfig
import com.shakster.borgar.core.media.processMedia

abstract class MediaProcessingTask(
    protected val maxFileSize: Long,
) : MappedFileTask() {

    abstract val config: MediaProcessingConfig

    override suspend fun process(input: DataSource): DataSource =
        processMedia(input, config, maxFileSize)

    override fun then(after: FileTask): FileTask {
        if (!after.requireInput) {
            throw UnsupportedOperationException("The task after this one must require input")
        }
        return if (after is MediaProcessingTask) {
            ChainedMediaProcessingTask(this, after, maxFileSize)
        } else {
            super.then(after)
        }
    }
}

class ChainedMediaProcessingTask(
    val first: MediaProcessingTask,
    val second: MediaProcessingTask,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = first.config then second.config

    override fun toString(): String {
        return "ChainedMediaProcessingTask(maxFileSize=$maxFileSize, config=$config)"
    }
}
