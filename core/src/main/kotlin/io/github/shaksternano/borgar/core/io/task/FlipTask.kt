package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.media.MediaProcessConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessConfig
import io.github.shaksternano.borgar.core.media.flipX
import io.github.shaksternano.borgar.core.media.flipY

class FlipTask(
    private val vertical: Boolean,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessConfig = SimpleMediaProcessConfig("flipped") {
        val image = it.content
        if (vertical) {
            image.flipY()
        } else {
            image.flipX()
        }
    }
}
