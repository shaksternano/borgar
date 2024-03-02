package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.AudioReaderFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.time.Duration

object NoAudioReader : BaseAudioReader() {

    override val frameCount: Int = 0
    override val frameRate: Double = 0.0
    override val duration: Duration = Duration.ZERO
    override val frameDuration: Duration = Duration.ZERO
    override val audioChannels: Int = 0
    override val audioSampleRate: Int = 0
    override val audioBitrate: Int = 0
    override val loopCount: Int = 0

    override suspend fun readFrame(timestamp: Duration): AudioFrame =
        throw UnsupportedOperationException("No audio available")

    override fun asFlow(): Flow<AudioFrame> = emptyFlow()

    override suspend fun reversed(): MediaReader<AudioFrame> = this

    override suspend fun createChangedSpeed(speedMultiplier: Double): MediaReader<AudioFrame> = this

    override suspend fun close() = Unit

    class Factory(
        override val supportedFormats: Set<String>
    ) : AudioReaderFactory {
        override suspend fun create(input: DataSource): AudioReader = NoAudioReader
    }
}
