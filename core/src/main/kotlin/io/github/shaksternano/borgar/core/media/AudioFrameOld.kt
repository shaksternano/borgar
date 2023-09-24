package io.github.shaksternano.borgar.core.media

import org.bytedeco.javacv.Frame

data class AudioFrameOld(
    override val content: Frame,
    override val duration: Double,
    override val timestamp: Long
) : VideoFrameOld<Frame, AudioFrameOld> {

    override fun transform(newContent: Frame): AudioFrameOld {
        return transform(newContent, 1F)
    }

    override fun transform(speedMultiplier: Float): AudioFrameOld {
        return transform(content, speedMultiplier)
    }

    override fun transform(newContent: Frame, speedMultiplier: Float): AudioFrameOld {
        newContent.sampleRate = (newContent.sampleRate * speedMultiplier).toInt()
        return AudioFrameOld(
            newContent,
            duration / speedMultiplier,
            timestamp
        )
    }

    override fun transform(duration: Double, timestamp: Long): AudioFrameOld {
        return AudioFrameOld(content, duration, timestamp)
    }
}
