package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.MediaProcessConfig
import io.github.shaksternano.borgar.core.media.processMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

abstract class MediaProcessingTask(
    private val maxFileSize: Long,
) : FileTask {

    override val requireInput: Boolean = true
    protected abstract val config: MediaProcessConfig
    private val filesToDelete: MutableList<Path> = mutableListOf()

    override suspend fun run(input: List<DataSource>): List<DataSource> {
        return input.map {
            val path = it.getOrWriteFile()
            if (!it.fromFile) {
                filesToDelete.add(path)
            }
            val output = processMedia(path, config, maxFileSize)
            return listOf(DataSource.fromFile(output.name, output.path))
        }
    }

    override fun then(after: FileTask): FileTask {
        return if (after is MediaProcessingTask) {
            object : MediaProcessingTask(maxFileSize) {
                override val config: MediaProcessConfig = this@MediaProcessingTask.config then after.config
            }
        } else {
            super.then(after)
        }
    }

    override suspend fun cleanup() {
        withContext(Dispatchers.IO) {
            filesToDelete.forEach {
                it.deleteIfExists()
            }
        }
        filesToDelete.clear()
    }
}
