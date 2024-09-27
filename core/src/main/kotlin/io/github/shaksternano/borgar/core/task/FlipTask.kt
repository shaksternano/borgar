package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.media.MediaProcessingConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessingConfig
import io.github.shaksternano.borgar.core.media.flipX
import io.github.shaksternano.borgar.core.media.flipY

class FlipTask(
    private val vertical: Boolean,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SimpleMediaProcessingConfig {
        val image = it.content
        if (vertical) {
            image.flipY()
        } else {
            image.flipX()
        }
    }
}
