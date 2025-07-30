package com.shakster.borgar.core.task

import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.io.fileExtension
import com.shakster.borgar.core.io.filenameWithoutExtension
import com.shakster.borgar.core.media.MediaProcessingConfig
import com.shakster.borgar.core.media.isStaticOnly
import com.shakster.borgar.core.media.processMedia

class GifTask(
    private val forceTranscode: Boolean,
    private val forceRename: Boolean,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = GifConfig(
        forceTranscode = forceTranscode,
        forceRename = forceRename,
    )

    override suspend fun process(input: DataSource): DataSource {
        return if (forceTranscode || !(forceRename || isStaticOnly(input.fileExtension))) {
            val config = TranscodeConfig("gif")
            processMedia(input, config, maxFileSize)
        } else {
            input.rename(input.filenameWithoutExtension + ".gif")
        }
    }
}

private class GifConfig(
    private val forceTranscode: Boolean,
    private val forceRename: Boolean,
) : MediaProcessingConfig {

    override val outputExtension: String = "gif"

    override fun transformOutputFormat(inputFormat: String): String {
        return if (forceTranscode || !(forceRename || isStaticOnly(inputFormat))) {
            "gif"
        } else {
            inputFormat
        }
    }
}
