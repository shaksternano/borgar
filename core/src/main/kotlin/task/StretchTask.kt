package com.shakster.borgar.core.task

import com.shakster.borgar.core.media.ImageFrame
import com.shakster.borgar.core.media.stretch
import java.awt.image.BufferedImage

class StretchTask(
    private val widthMultiplier: Double,
    private val heightMultiplier: Double,
    private val raw: Boolean,
    maxFileSize: Long,
) : SimpleMediaProcessingTask(maxFileSize) {

    override suspend fun transformImage(frame: ImageFrame): BufferedImage {
        val image = frame.content
        return image.stretch(
            (image.width * widthMultiplier).toInt(),
            (image.height * heightMultiplier).toInt(),
            raw,
        )
    }
}
