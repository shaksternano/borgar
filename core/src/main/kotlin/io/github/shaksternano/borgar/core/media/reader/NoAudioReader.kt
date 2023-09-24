package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.CloseableIterator
import io.github.shaksternano.borgar.core.collect.CloseableSpliterator
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.AudioReaderFactory
import java.util.function.Consumer

object NoAudioReader : BaseAudioReader() {

    override val size: Int = 0
    override val frameRate: Double = 0.0
    override val duration: Double = 0.0
    override val frameDuration: Double = 0.0
    override val audioChannels: Int = 0
    override val audioSampleRate: Int = 0
    override val audioBitrate: Int = 0
    override val loopCount: Int = 0

    override fun readFrame(timestamp: Double): AudioFrame = throw UnsupportedOperationException("No audio available")

    override fun createReversed(): AudioReader = this

    override fun iterator(): CloseableIterator<AudioFrame> = CloseableIterator.empty()

    override fun forEach(action: Consumer<in AudioFrame>) = Unit

    override fun spliterator(): CloseableSpliterator<AudioFrame> = CloseableSpliterator.empty()

    override fun close() = Unit

    object Factory : AudioReaderFactory {
        override val supportedFormats: Set<String> = emptySet()

        override fun create(input: DataSource): AudioReader = NoAudioReader
    }
}
