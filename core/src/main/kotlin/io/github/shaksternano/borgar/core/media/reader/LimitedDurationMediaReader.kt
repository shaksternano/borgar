package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.media.VideoFrame
import io.github.shaksternano.borgar.core.util.circular
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlin.time.Duration

class LimitedDurationMediaReader<T : VideoFrame<*>> internal constructor(
    private val reader: MediaReader<T>,
    readerInfo: ReaderInfo,
) : BaseMediaReader<T>() {

    override val frameCount: Int = readerInfo.frameCount
    override val duration: Duration = readerInfo.duration
    override val frameDuration: Duration = duration / frameCount
    override val frameRate: Double = 1000.0 / frameDuration.inWholeMilliseconds
    override val audioChannels: Int = reader.audioChannels
    override val audioSampleRate: Int = reader.audioSampleRate
    override val audioBitrate: Int = reader.audioBitrate
    override val width: Int = reader.width
    override val height: Int = reader.height
    override val loopCount: Int = reader.loopCount

    override suspend fun readFrame(timestamp: Duration): T {
        val circularTimestamp = timestamp.circular(duration)
        return reader.readFrame(circularTimestamp)
    }

    override fun asFlow(): Flow<T> {
        var currentTimestamp = Duration.ZERO
        return reader.asFlow().takeWhile {
            if (currentTimestamp >= duration) return@takeWhile false
            currentTimestamp += it.duration
            true
        }
    }

    override fun createReversed(): MediaReader<T> =
        LimitedDurationMediaReader(
            reader.reversed,
            ReaderInfo(reader),
        )

    override suspend fun close() = reader.close()
}

@Suppress("FunctionName")
suspend fun <T : VideoFrame<*>> LimitedDurationMediaReader(
    reader: MediaReader<T>,
    duration: Duration,
): MediaReader<T> = LimitedDurationMediaReader(
    reader,
    readerInfo(reader, duration),
)

private suspend fun readerInfo(reader: MediaReader<*>, maxDuration: Duration): ReaderInfo {
    if (reader.isEmpty) return ReaderInfo(reader)
    var frameCount = 0
    var totalDuration = Duration.ZERO
    reader.asFlow().takeWhile {
        val newDuration = totalDuration + it.duration
        if (newDuration > maxDuration) {
            if (frameCount == 0) {
                frameCount = 1
                totalDuration = newDuration
            }
            return@takeWhile false
        }
        frameCount++
        totalDuration = newDuration
        return@takeWhile true
    }.collect()
    return ReaderInfo(frameCount, totalDuration)
}

internal data class ReaderInfo(val frameCount: Int, val duration: Duration) {
    constructor(reader: MediaReader<*>) : this(
        reader.frameCount,
        reader.duration,
    )
}
