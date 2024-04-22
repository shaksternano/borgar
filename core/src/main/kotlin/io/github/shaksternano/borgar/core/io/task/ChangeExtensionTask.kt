package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.fileExtension
import io.github.shaksternano.borgar.core.io.filename
import io.github.shaksternano.borgar.core.io.filenameWithoutExtension

class ChangeExtensionTask(
    private val newExtension: String,
) : MappedFileTask() {

    override suspend fun process(input: DataSource): DataSource {
        if (input.fileExtension == newExtension && !input.sendUrl && input.url != null) {
            return input.withSendUrl(true)
        }
        val filenameWithoutExtension = input.filenameWithoutExtension
        val newFilename = filename(filenameWithoutExtension, newExtension)
        return input.rename(newFilename)
    }
}
