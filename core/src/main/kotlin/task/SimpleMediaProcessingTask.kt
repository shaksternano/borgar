package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageProcessor
import io.github.shaksternano.borgar.core.media.MediaProcessingConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessingConfig
import kotlinx.coroutines.flow.Flow
import java.awt.image.BufferedImage

abstract class SimpleMediaProcessingTask(
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    final override val config: MediaProcessingConfig = SimpleMediaProcessingConfig(
        SimpleImageProcessor(::transformImage),
    )

    abstract suspend fun transformImage(frame: ImageFrame): BufferedImage
}

private class SimpleImageProcessor(
    private val transform: suspend (ImageFrame) -> BufferedImage,
) : ImageProcessor<Unit> {

    override suspend fun constantData(firstFrame: ImageFrame, imageSource: Flow<ImageFrame>, outputFormat: String) =
        Unit

    override suspend fun transformImage(frame: ImageFrame, constantData: Unit): BufferedImage =
        transform(frame)

    override fun toString(): String {
        return "SimpleImageProcessor(transform=$transform)"
    }
}
