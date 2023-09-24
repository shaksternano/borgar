package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.elementsEqual
import io.github.shaksternano.borgar.core.collect.hashElements
import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.VideoFrame

abstract class BaseMediaReader<T : VideoFrame<*>> : MediaReader<T> {

    final override val reversed: MediaReader<T> by lazy(::createReversed)

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
