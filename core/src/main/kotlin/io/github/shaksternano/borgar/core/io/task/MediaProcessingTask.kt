package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.MediaProcessConfig
import io.github.shaksternano.borgar.core.media.processMedia

class MediaProcessingTask(
    private val maxFileSize: Long,
    private val config: MediaProcessConfig,
) : BaseFileTask() {

    override val requireInput: Boolean = true

    override suspend fun run(input: List<DataSource>): List<DataSource> = input.map {
        val path = it.getOrWriteFile().path
        val output = processMedia(path, config, maxFileSize)
        outputs.add(output.path)
        output
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
