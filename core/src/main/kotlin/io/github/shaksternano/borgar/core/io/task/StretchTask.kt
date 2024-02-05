package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.media.MediaProcessConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessConfig
import io.github.shaksternano.borgar.core.media.stretch

class StretchTask(
    widthMultiplier: Double,
    heightMultiplier: Double,
    raw: Boolean,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessConfig = SimpleMediaProcessConfig("stretched") {
        val image = it.content
        image.stretch(
            (image.width * widthMultiplier).toInt(),
            (image.height * heightMultiplier).toInt(),
            raw,
        )
    }
}
