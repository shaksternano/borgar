package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.stretch
import java.awt.image.BufferedImage

class PixelateTask(
    private val pixelationMultiplier: Double,
    maxFileSize: Long,
) : SimpleMediaProcessingTask(maxFileSize) {

    override suspend fun transformImage(frame: ImageFrame): BufferedImage {
        val image = frame.content
        return image.stretch(
            (image.width / pixelationMultiplier).toInt(),
            (image.height / pixelationMultiplier).toInt(),
            true,
        ).stretch(
            image.width,
            image.height,
            true,
        )
    }
}
