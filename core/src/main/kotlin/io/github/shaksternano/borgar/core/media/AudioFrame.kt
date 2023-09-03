package io.github.shaksternano.borgar.core.media

import org.bytedeco.javacv.Frame

data class AudioFrame(
    override val content: Frame,
    override val duration: Double,
    override val timestamp: Long
) : VideoFrame<Frame, AudioFrame> {

    override fun transform(newContent: Frame): AudioFrame {
        return transform(newContent, 1F)
    }

    override fun transform(speedMultiplier: Float): AudioFrame {
        return transform(content, speedMultiplier)
    }

    override fun transform(newContent: Frame, speedMultiplier: Float): AudioFrame {
        newContent.sampleRate = (newContent.sampleRate * speedMultiplier).toInt()
        return AudioFrame(
            newContent,
            duration / speedMultiplier,
            timestamp
        )
    }

    override fun transform(duration: Double, timestamp: Long): AudioFrame {
        return AudioFrame(content, duration, timestamp)
    }
}
