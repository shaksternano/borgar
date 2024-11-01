package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.media.*
import java.awt.image.BufferedImage

class InvertColorsTask(
    maxFileSize: Long,
) : SimpleMediaProcessingTask(maxFileSize) {

    override suspend fun transformImage(frame: ImageFrame): BufferedImage {
        return frame.content.mapPixels { rgb ->
            val red = rgb shr 16 and 0xFF
            val green = rgb shr 8 and 0xFF
            val blue = rgb and 0xFF
            val alpha = rgb shr 24 and 0xFF
            val invertedRed = 255 - red
            val invertedGreen = 255 - green
            val invertedBlue = 255 - blue
            alpha shl 24 or (invertedRed shl 16) or (invertedGreen shl 8) or invertedBlue
        }
    }
}
