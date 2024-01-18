package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.*

class ChangeExtensionTask(
    private val newExtension: String,
    private val maxFileSize: Long,
) : MappedFileTask(true) {

    override suspend fun process(input: DataSource): DataSource {
        if (input.fileExtension() == newExtension && !input.sendUrl && input.url != null) {
            return input.withSendUrl(true)
        }
        if (input.size() > maxFileSize) {
            throw ErrorResponseException("File is too large! (Max: ${toMb(maxFileSize)}MB)")
        }
        val filenameWithoutExtension = input.filenameWithoutExtension()
        val newFilename = filename(filenameWithoutExtension, newExtension)
        return input.rename(newFilename)
    }
}
