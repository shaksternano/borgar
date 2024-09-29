package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.media.VideoFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.time.Duration

class ConstantFrameDurationMediaReader<T : VideoFrame<*>>(
    private val reader: MediaReader<T>,
    override val frameDuration: Duration,
    totalDuration: Duration,
) : BaseMediaReader<T>() {

    constructor(reader: MediaReader<T>, frameDuration: Duration) :
        this(reader, frameDuration, reader.duration)

    override val frameCount: Int = ceil(totalDuration / frameDuration).toInt()
    override val frameRate: Double = 1000.0 / frameDuration.inWholeMilliseconds
    override val duration: Duration = frameDuration * frameCount
    override val audioChannels: Int = reader.audioChannels
    override val audioSampleRate: Int = reader.audioSampleRate
    override val audioBitrate: Int = reader.audioBitrate
    override val width: Int = reader.width
    override val height: Int = reader.height
    override val loopCount: Int = reader.loopCount

    override suspend fun readFrame(timestamp: Duration): T {
        val frameNumber = floor(timestamp / frameDuration)
        val newTimestamp = (frameDuration * frameNumber)
        @Suppress("UNCHECKED_CAST")
        return reader.readFrame(timestamp).copy(
            duration = frameDuration,
            timestamp = newTimestamp,
        ) as T
    }

    override fun asFlow(): Flow<T> = flow {
        var currentTimestamp = Duration.ZERO
        // Use do-while loop in case duration is 0
        do {
            @Suppress("UNCHECKED_CAST")
            val frame = reader.readFrame(currentTimestamp).copy(
                duration = frameDuration,
                timestamp = currentTimestamp,
            ) as T
            emit(frame)
            currentTimestamp += frameDuration
        } while (currentTimestamp < duration)
    }

    override suspend fun close() = reader.close()

    override fun toString(): String {
        return "ConstantFrameDurationMediaReader(" +
            "reader=$reader" +
            ", frameDuration=$frameDuration" +
            ", frameCount=$frameCount" +
            ")"
    }
}
