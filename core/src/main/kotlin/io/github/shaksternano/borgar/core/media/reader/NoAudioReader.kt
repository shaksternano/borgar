package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.CloseableIterator
import io.github.shaksternano.borgar.core.collect.CloseableSpliterator
import io.github.shaksternano.borgar.core.media.AudioFrame
import java.util.function.Consumer

object NoAudioReader : BaseMediaReader<AudioFrame>() {

    override val size: Long = 0
    override val frameRate: Double = 0.0
    override val duration: Double = 0.0
    override val frameDuration: Double = 0.0
    override val audioChannels: Int = 0
    override val audioSampleRate: Int = 0
    override val audioBitrate: Int = 0
    override val width: Int = 0
    override val height: Int = 0
    override val loopCount: Int = 0
    override val format: String = ""

    override suspend fun start() = Unit

    override fun readFrame(timestamp: Double): AudioFrame = throw UnsupportedOperationException("No audio available")

    override fun createReversed(): MediaReader<AudioFrame> = this

    override fun iterator(): CloseableIterator<AudioFrame> = CloseableIterator.empty()

    override fun forEach(action: Consumer<in AudioFrame>) = Unit

    override fun spliterator(): CloseableSpliterator<AudioFrame> = CloseableSpliterator.empty()

    override fun close() = Unit
}
