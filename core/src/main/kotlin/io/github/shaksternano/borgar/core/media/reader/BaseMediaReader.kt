package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.VideoFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.time.Duration

abstract class BaseMediaReader<T : VideoFrame<*>> : MediaReader<T> {

    override val reversed: MediaReader<T> by lazy(::createReversed)

    protected abstract fun createReversed(): MediaReader<T>

    final override fun changeSpeed(speedMultiplier: Double): MediaReader<T> =
        if (speedMultiplier == 1.0) this
        else if (speedMultiplier == 0.0) throw IllegalArgumentException("Speed multiplier cannot be 0")
        else if (speedMultiplier < 0.0) reversed.changeSpeed(abs(speedMultiplier))
        else createChangedSpeed(speedMultiplier)

    protected open fun createChangedSpeed(speedMultiplier: Double): MediaReader<T> =
        ChangedSpeedReader(this, speedMultiplier)
}

open class ChangedSpeedReader<T : VideoFrame<*>>(
    private val reader: MediaReader<T>,
    protected val speedMultiplier: Double,
) : MediaReader<T> by reader {

    override val frameRate: Double = reader.frameRate * speedMultiplier
    override val duration: Duration = reader.duration / speedMultiplier
    override val frameDuration: Duration = reader.frameDuration / speedMultiplier
    override val reversed: MediaReader<T> by lazy {
        val unmodifiedReverse = reader.reversed
        if (unmodifiedReverse === reader) this
        else unmodifiedReverse.changeSpeed(speedMultiplier)
    }

    override suspend fun readFrame(timestamp: Duration): T {
        val newTimestamp = timestamp / speedMultiplier
        return reader.readFrame(newTimestamp)
    }

    override fun asFlow(): Flow<T> =
        reader.asFlow()
            .map {
                it.copy(
                    timestamp = it.timestamp / speedMultiplier,
                    duration = it.duration / speedMultiplier,
                )
            }
            .let {
                @Suppress("UNCHECKED_CAST")
                it as Flow<T>
            }
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

    override fun createChangedSpeed(speedMultiplier: Double): MediaReader<AudioFrame> =
        ChangedSpeedAudioReader(this, speedMultiplier)
}

class ChangedSpeedAudioReader(
    reader: AudioReader,
    speedMultiplier: Double,
) : ChangedSpeedReader<AudioFrame>(
    reader,
    speedMultiplier,
) {

    override suspend fun readFrame(timestamp: Duration): AudioFrame =
        super.readFrame(timestamp).also {
            it.changeSampleRate(speedMultiplier)
        }

    override fun asFlow(): Flow<AudioFrame> =
        super.asFlow()
            .map {
                it.changeSampleRate(speedMultiplier)
            }
}

private fun AudioFrame.changeSampleRate(speedMultiplier: Double): AudioFrame {
    content.sampleRate = (content.sampleRate * speedMultiplier).toInt()
    return this
}

abstract class ReversedReader<T : VideoFrame<*>>(
    private val reader: MediaReader<T>,
) : BaseMediaReader<T>() {

    final override val frameCount: Int = reader.frameCount
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

    override suspend fun close() = reader.close()
}

abstract class ReversedImageReader(
    reader: ImageReader,
) : ReversedReader<ImageFrame>(reader), ImageReader

abstract class ReversedAudioReader(
    reader: AudioReader,
) : ReversedReader<AudioFrame>(reader), AudioReader {

    override fun createChangedSpeed(speedMultiplier: Double): MediaReader<AudioFrame> =
        ChangedSpeedAudioReader(this, speedMultiplier)
}
