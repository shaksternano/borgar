package com.shakster.borgar.core.task

import com.shakster.borgar.core.media.ImageFrame
import com.shakster.borgar.core.media.flipX
import com.shakster.borgar.core.media.flipY
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
