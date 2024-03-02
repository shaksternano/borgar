package io.github.shaksternano.borgar.core.media

import org.bytedeco.javacv.Frame
import java.awt.image.BufferedImage
import kotlin.time.Duration

data class VideoFrame<T>(
    val content: T,
    val duration: Duration,
    val timestamp: Duration,
)

typealias ImageFrame = VideoFrame<BufferedImage>
typealias AudioFrame = VideoFrame<Frame>

interface VideoFrameOld<T, V : VideoFrameOld<T, V>> {

    /**
     * The content of the frame.
     *
     * @return The content of the frame.
     */
    val content: T

    /**
     * The duration of the frame in microseconds.
     *
     * @return The duration of the frame in microseconds.
     */
    val duration: Double

    /**
     * The timestamp of the frame in microseconds.
     *
     * @return The timestamp of the frame in microseconds.
     */
    val timestamp: Long

    fun transform(newContent: T): V

    fun transform(speedMultiplier: Float): V

    fun transform(newContent: T, speedMultiplier: Float): V

    fun transform(duration: Double, timestamp: Long): V
}
