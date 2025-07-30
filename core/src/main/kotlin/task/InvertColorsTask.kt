package com.shakster.borgar.core.task

import com.shakster.borgar.core.media.ImageFrame
import com.shakster.borgar.core.media.mapPixels
import java.awt.image.BufferedImage

class InvertColorsTask(
    maxFileSize: Long,
) : SimpleMediaProcessingTask(maxFileSize) {

    override suspend fun transformImage(frame: ImageFrame): BufferedImage {
        return frame.content.mapPixels { rgb ->
            val red = rgb shr 16 and 0xFF
            val green = rgb shr 8 and 0xFF
            val blue = rgb and 0xFF
            val alpha = rgb ushr 24
            val invertedRed = 255 - red
            val invertedGreen = 255 - green
            val invertedBlue = 255 - blue
            alpha shl 24 or (invertedRed shl 16) or (invertedGreen shl 8) or invertedBlue
        }
    }
}
