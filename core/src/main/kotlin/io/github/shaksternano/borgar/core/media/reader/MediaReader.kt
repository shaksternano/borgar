package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.CloseableIterable
import io.github.shaksternano.borgar.core.collect.CloseableSpliterator
import io.github.shaksternano.borgar.core.collect.SizedIterable
import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.VideoFrame
import java.util.*

interface MediaReader<T : VideoFrame<*>> : CloseableIterable<T>, SizedIterable<T> {

    /**
     * The frame rate in frames per second.
     */
    val frameRate: Double

    /**
     * The duration in microseconds.
     */
    val duration: Double

    /**
     * The duration of each frame in microseconds.
     */
    val frameDuration: Double

    val audioChannels: Int
    val audioSampleRate: Int
    val audioBitrate: Int

    val width: Int
    val height: Int
    val loopCount: Int

    val format: String

    val reversed: MediaReader<T>

    suspend fun start()

    /**
     * Gets the frame at the given timestamp.
     * If the timestamp is larger than the duration of the media,
     * the reader will wrap around to the beginning.
     *
     * @param timestamp The timestamp in microseconds.
     * @return The frame at the given timestamp.
     */
    fun readFrame(timestamp: Double): T

    override fun spliterator(): CloseableSpliterator<T> {
        val characteristics = (
            Spliterator.ORDERED
                or Spliterator.DISTINCT
                or Spliterator.SORTED
                or Spliterator.NONNULL
                or Spliterator.IMMUTABLE
            )
        return CloseableSpliterator.create(
            iterator(),
            frameCount,
            characteristics,
        )
    }
}

val MediaReader<*>.frameCount: Long
    get() = size
val MediaReader<*>.isEmpty: Boolean
    get() = size == 0L
val MediaReader<*>.isAnimated: Boolean
    get() = size > 1
val <T : VideoFrame<*>> MediaReader<T>.first: T
    get() = readFrame(0.0)

typealias ImageReader = MediaReader<ImageFrame>
typealias AudioReader = MediaReader<AudioFrame>
