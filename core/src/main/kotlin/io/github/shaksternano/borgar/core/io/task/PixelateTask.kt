package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.media.MediaProcessConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessConfig
import io.github.shaksternano.borgar.core.media.stretch

class PixelateTask(
    private val pixelationMultiplier: Double,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessConfig = SimpleMediaProcessConfig("pixelated") {
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
