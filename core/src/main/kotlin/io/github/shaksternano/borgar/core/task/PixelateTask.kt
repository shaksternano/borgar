package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.media.MediaProcessingConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessingConfig
import io.github.shaksternano.borgar.core.media.stretch

class PixelateTask(
    private val pixelationMultiplier: Double,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SimpleMediaProcessingConfig("pixelated") {
        val image = it.content
        image.stretch(
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
