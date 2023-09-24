package io.github.shaksternano.borgar.core.media

import java.awt.image.BufferedImage

data class ImageFrameOld(
    override val content: BufferedImage,
    override val duration: Double,
    override val timestamp: Long
) : VideoFrameOld<BufferedImage, ImageFrameOld> {

    override fun transform(newContent: BufferedImage): ImageFrameOld {
        return transform(newContent, 1F)
    }

    override fun transform(speedMultiplier: Float): ImageFrameOld {
        return transform(content, speedMultiplier)
    }

    override fun transform(newContent: BufferedImage, speedMultiplier: Float): ImageFrameOld {
        return ImageFrameOld(
            newContent,
            duration / speedMultiplier,
            timestamp
        )
    }

    override fun transform(duration: Double, timestamp: Long): ImageFrameOld {
        return ImageFrameOld(content, duration, timestamp)
    }
}
