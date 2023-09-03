package io.github.shaksternano.borgar.core.media

import java.awt.image.BufferedImage

data class ImageFrame(
    override val content: BufferedImage,
    override val duration: Double,
    override val timestamp: Long
) : VideoFrame<BufferedImage, ImageFrame> {

    override fun transform(newContent: BufferedImage): ImageFrame {
        return transform(newContent, 1F)
    }

    override fun transform(speedMultiplier: Float): ImageFrame {
        return transform(content, speedMultiplier)
    }

    override fun transform(newContent: BufferedImage, speedMultiplier: Float): ImageFrame {
        return ImageFrame(
            newContent,
            duration / speedMultiplier,
            timestamp
        )
    }

    override fun transform(duration: Double, timestamp: Long): ImageFrame {
        return ImageFrame(content, duration, timestamp)
    }
}
