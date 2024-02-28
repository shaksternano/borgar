package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.media.MediaProcessingConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessingConfig
import io.github.shaksternano.borgar.core.media.stretch

class StretchTask(
    widthMultiplier: Double,
    heightMultiplier: Double,
    raw: Boolean,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SimpleMediaProcessingConfig("stretched") {
        val image = it.content
        image.stretch(
            (image.width * widthMultiplier).toInt(),
            (image.height * heightMultiplier).toInt(),
            raw,
        )
    }
}
