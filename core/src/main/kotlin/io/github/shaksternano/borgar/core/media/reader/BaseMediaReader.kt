package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.elementsEqual
import io.github.shaksternano.borgar.core.collect.hashElements
import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.VideoFrame
import kotlin.time.Duration

abstract class BaseMediaReader<T : VideoFrame<*>> : MediaReader<T> {

    override val reversed: MediaReader<T> by lazy(::createReversed)

    protected abstract fun createReversed(): MediaReader<T>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is MediaReader<*>) return elementsEqual(other)
        return false
    }

    override fun hashCode(): Int = hashElements()
}

abstract class BaseImageReader : BaseMediaReader<ImageFrame>() {
    final override val audioChannels: Int = 0
    final override val audioSampleRate: Int = 0
    final override val audioBitrate: Int = 0

    abstract override fun createReversed(): ImageReader
}

abstract class BaseAudioReader : BaseMediaReader<AudioFrame>() {
    final override val width: Int = 0
    final override val height: Int = 0
    abstract override fun createReversed(): AudioReader
}

abstract class ReversedReader<T : VideoFrame<*>>(
    private val reader: MediaReader<T>,
) : BaseMediaReader<T>() {

    final override val size: Int = reader.size
    final override val frameRate: Double = reader.frameRate
    final override val duration: Duration = reader.duration
    final override val frameDuration: Duration = reader.frameDuration
    final override val audioChannels: Int = reader.audioChannels
    final override val audioSampleRate: Int = reader.audioSampleRate
    final override val audioBitrate: Int = reader.audioBitrate
    final override val width: Int = reader.width
    final override val height: Int = reader.height
    final override val loopCount: Int = reader.loopCount
    final override val reversed: MediaReader<T> = reader

    final override fun createReversed(): MediaReader<T> = reader

    override fun close() = reader.close()
}

abstract class ReversedImageReader(
    reader: ImageReader,
) : ReversedReader<ImageFrame>(reader), ImageReader

abstract class ReversedAudioReader(
    reader: AudioReader,
) : ReversedReader<AudioFrame>(reader), AudioReader
