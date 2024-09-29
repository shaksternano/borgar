package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.media.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

abstract class BaseMediaReader<T : VideoFrame<*>> : MediaReader<T> {

    override suspend fun reversed(): MediaReader<T> =
        ReversedReader(
            this,
            createReversedFrameInfo()
        )

    private suspend fun createReversedFrameInfo(): List<ReversedFrameInfo> = buildList {
        val frameInfo = asFlow().map { (_, duration, timestamp) ->
            FrameInfo(duration, timestamp)
        }.toList()
        frameInfo.reversed().fold(Duration.ZERO) { timestamp, frame ->
            add(ReversedFrameInfo(frame.duration, frame.timestamp, timestamp))
            timestamp + frame.duration
        }
    }

    final override suspend fun changeSpeed(speedMultiplier: Double): MediaReader<T> =
        if (speedMultiplier == 1.0) this
        else if (speedMultiplier == 0.0) throw IllegalArgumentException("Speed multiplier cannot be 0")
        else if (speedMultiplier < 0.0) reversed().changeSpeed(abs(speedMultiplier))
        else createChangedSpeed(speedMultiplier)

    protected open suspend fun createChangedSpeed(speedMultiplier: Double): MediaReader<T> =
        ChangedSpeedReader(this, speedMultiplier)
}

private class ReversedReader<T : VideoFrame<*>>(
    private val reader: MediaReader<T>,
    private val reversedFrameInfo: List<ReversedFrameInfo>,
) : BaseMediaReader<T>() {

    override val frameCount: Int = reader.frameCount
    override val frameRate: Double = reader.frameRate
    override val duration: Duration = reader.duration
    override val frameDuration: Duration = reader.frameDuration
    override val audioChannels: Int = reader.audioChannels
    override val audioSampleRate: Int = reader.audioSampleRate
    override val audioBitrate: Int = reader.audioBitrate
    override val width: Int = reader.width
    override val height: Int = reader.height
    override val loopCount: Int = reader.loopCount

    override suspend fun readFrame(timestamp: Duration): T {
        val circularTimestamp =
            (timestamp.inWholeMilliseconds % duration.inWholeMilliseconds.coerceAtLeast(1)).milliseconds
        val index = findIndex(circularTimestamp, reversedFrameInfo.map(ReversedFrameInfo::reversedTimestamp))
        val frameInfo = reversedFrameInfo[index]
        val frame = reader.readFrame(frameInfo.actualTimestamp)
        @Suppress("UNCHECKED_CAST")
        return frame.copy(
            timestamp = frameInfo.reversedTimestamp,
        ) as T
    }

    override fun asFlow(): Flow<T> = flow {
        reversedFrameInfo.forEach {
            @Suppress("UNCHECKED_CAST")
            val frame = reader.readFrame(it.actualTimestamp).copy(
                timestamp = it.reversedTimestamp
            ) as T
            emit(frame)
        }
    }

    override suspend fun reversed(): MediaReader<T> = reader

    override suspend fun close() = reader.close()

    override fun toString(): String {
        return "ReversedReader(" +
            "reader=$reader" +
            ", reversedFrameInfo=$reversedFrameInfo" +
            ")"
    }
}

private data class ReversedFrameInfo(
    val duration: Duration,
    val actualTimestamp: Duration,
    val reversedTimestamp: Duration,
)

open class ChangedSpeedReader<T : VideoFrame<*>>(
    private val reader: MediaReader<T>,
    private val speedMultiplier: Double,
) : BaseMediaReader<T>() {

    override val frameCount: Int = reader.frameCount
    override val frameRate: Double = reader.frameRate * speedMultiplier
    override val duration: Duration = reader.duration / speedMultiplier
    override val frameDuration: Duration = reader.frameDuration / speedMultiplier
    override val audioChannels: Int = reader.audioChannels
    override val audioSampleRate: Int = reader.audioSampleRate
    override val audioBitrate: Int = reader.audioBitrate
    override val width: Int = reader.width
    override val height: Int = reader.height
    override val loopCount: Int = reader.loopCount

    override suspend fun readFrame(timestamp: Duration): T {
        val newTimestamp = timestamp * speedMultiplier
        val frame = reader.readFrame(newTimestamp)
        @Suppress("UNCHECKED_CAST")
        return frame.copy(
            timestamp = frame.timestamp / speedMultiplier,
            duration = frame.duration / speedMultiplier,
        ) as T
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

    override suspend fun close() = reader.close()

    override fun toString(): String {
        return "ChangedSpeedReader(" +
            "reader=$reader" +
            ", speedMultiplier=$speedMultiplier" +
            ")"
    }
}

abstract class BaseImageReader : BaseMediaReader<ImageFrame>() {

    final override val audioChannels: Int = 0
    final override val audioSampleRate: Int = 0
    final override val audioBitrate: Int = 0
}

abstract class BaseAudioReader : BaseMediaReader<AudioFrame>() {

    final override val width: Int = 0
    final override val height: Int = 0
}
