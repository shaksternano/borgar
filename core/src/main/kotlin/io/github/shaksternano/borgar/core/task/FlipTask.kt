package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.flipX
import io.github.shaksternano.borgar.core.media.flipY
import java.awt.image.BufferedImage

class FlipTask(
    private val vertical: Boolean,
    maxFileSize: Long,
) : SimpleMediaProcessingTask(maxFileSize) {

    override suspend fun transformImage(frame: ImageFrame): BufferedImage {
        val image = frame.content
        return if (vertical) {
            image.flipY()
        } else {
            image.flipX()
        }
    }
}
