package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.media.IdentityImageProcessor
import io.github.shaksternano.borgar.core.media.ImageProcessor
import io.github.shaksternano.borgar.core.media.MediaProcessConfig
import io.github.shaksternano.borgar.core.media.reader.ImageReader

class ReverseTask(
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessConfig = ReverseConfig
}

private object ReverseConfig : MediaProcessConfig {

    override val processor: ImageProcessor<out Any> = IdentityImageProcessor
    override val outputName: String = "reversed"

    override fun transformImageReader(imageReader: ImageReader): ImageReader =
        imageReader.reversed
}
