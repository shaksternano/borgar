package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.forEachNotNull
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.IO_DISPATCHER
import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.AudioReaderFactory
import io.github.shaksternano.borgar.core.media.frameAtTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.bytedeco.javacv.FFmpegFrameFilter
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds

class FFmpegAudioReader(
    input: Path,
    isTempFile: Boolean,
    grabber: FFmpegFrameGrabber,
    override val frameCount: Int,
    override val frameRate: Double,
    override val duration: Duration,
    override val frameDuration: Duration,
    override val audioChannels: Int,
    override val audioSampleRate: Int,
    audioBitrate: Int,
) : FFmpegMediaReader<AudioFrame>(
    input,
    grabber,
    isTempFile,
) {

    override val audioBitrate: Int = if (audioChannels > 0 && audioBitrate == 0) {
        128000
    } else {
        audioBitrate
    }
    override val width: Int = 0
    override val height: Int = 0

    override suspend fun setTimestamp(timestamp: Duration) = withContext(IO_DISPATCHER) {
        grabber.setAudioTimestamp(timestamp.inWholeMicroseconds)
    }

    override suspend fun grabFrame(grabber: FFmpegFrameGrabber): Frame? = withContext(IO_DISPATCHER) {
        grabber.grabSamples()
    }

    override fun convertFrame(frame: Frame): AudioFrame = AudioFrame(
        frame,
        frameDuration,
        frame.timestamp.microseconds,
    )

    override suspend fun reversed(): MediaReader<AudioFrame> =
        ReversedAudioReader(
            this,
            filterReverse(this),
        )

    override suspend fun createChangedSpeed(speedMultiplier: Double): MediaReader<AudioFrame> =
        ChangedSpeedAudioReader(
            this,
            speedMultiplier,
            filterChangeSpeed(this, speedMultiplier),
        )

    object Factory : AudioReaderFactory {

        // This is the default audio reader factory
        override val supportedFormats: Set<String> = setOf()

        override suspend fun create(input: DataSource): AudioReader =
            createReader(input) { path, isTempFile, grabber, frameCount, frameRate, frameDuration ->
                FFmpegAudioReader(
                    path,
                    isTempFile,
                    grabber,
                    frameCount,
                    frameRate,
                    grabber.timestamp.microseconds,
                    frameDuration,
                    grabber.audioChannels,
                    grabber.sampleRate,
                    grabber.audioBitrate,
                )
            }
    }
}

private class ReversedAudioReader(
    private val reader: AudioReader,
    private val reversedFrames: List<AudioFrame>,
) : BaseAudioReader() {

    override val frameCount: Int = reader.frameCount
    override val frameRate: Double = reader.frameRate
    override val duration: Duration = reader.duration
    override val frameDuration: Duration = reader.frameDuration
    override val audioChannels: Int = reader.audioChannels
    override val audioSampleRate: Int = reader.audioSampleRate
    override val audioBitrate: Int = reader.audioBitrate
    override val loopCount: Int = reader.loopCount

    override suspend fun readFrame(timestamp: Duration): AudioFrame =
        frameAtTime(timestamp, reversedFrames, duration)

    override fun asFlow(): Flow<AudioFrame> = flow {
        reversedFrames.forEach {
            emit(it)
        }
    }

    override suspend fun reversed(): MediaReader<AudioFrame> = reader

    override suspend fun createChangedSpeed(speedMultiplier: Double): MediaReader<AudioFrame> =
        ChangedSpeedAudioReader(
            this,
            speedMultiplier,
            filterChangeSpeed(this, speedMultiplier),
        )

    override suspend fun close() = reader.close()
}

private class ChangedSpeedAudioReader(
    private val reader: AudioReader,
    private val speedMultiplier: Double,
    private val changedSpeedFrames: List<AudioFrame>,
) : BaseAudioReader() {

    override val frameCount: Int = reader.frameCount
    override val frameRate: Double = reader.frameRate * speedMultiplier
    override val duration: Duration = reader.duration / speedMultiplier
    override val frameDuration: Duration = reader.frameDuration / speedMultiplier
    override val audioChannels: Int = reader.audioChannels
    override val audioSampleRate: Int = reader.audioSampleRate
    override val audioBitrate: Int = reader.audioBitrate
    override val loopCount: Int = reader.loopCount

    override suspend fun readFrame(timestamp: Duration): AudioFrame =
        frameAtTime(timestamp, changedSpeedFrames, duration)

    override fun asFlow(): Flow<AudioFrame> = flow {
        changedSpeedFrames.forEach {
            emit(it)
        }
    }

    override suspend fun reversed(): MediaReader<AudioFrame> =
        ReversedAudioReader(
            this,
            filterReverse(this),
        )

    override suspend fun createChangedSpeed(speedMultiplier: Double): MediaReader<AudioFrame> {
        val newSpeedMultiplier = this.speedMultiplier * speedMultiplier
        return ChangedSpeedAudioReader(
            reader,
            newSpeedMultiplier,
            filterChangeSpeed(reader, newSpeedMultiplier),
        )
    }

    override suspend fun close() = reader.close()
}

/**
 * Applies an FFmpeg audio filter to the audio frames.
 *
 * @param reader        The audio reader to read the audio frames from.
 * @param filter        The FFmpeg audio filter to apply.
 * @return The filtered audio frames.
 */
private suspend fun filterAudioFrames(
    reader: AudioReader,
    filter: String,
): List<AudioFrame> =
    FFmpegFrameFilter(filter, reader.audioChannels).use { frameFilter ->
        frameFilter.start()
        var sampleRate = -1
        // Pass the audio frames through the filter
        reader.asFlow().collect {
            frameFilter.push(it.content)
            // Remember the original sample rate
            if (sampleRate < 0)
                sampleRate = it.content.sampleRate
        }
        // Without this, pull() will always return null
        frameFilter.push(null)
        buildList {
            // Retrieve the filtered audio frames
            forEachNotNull(frameFilter::pull) {
                // The sample rate gets messed up by the filter, so we reset it
                it.sampleRate = sampleRate
                add(
                    AudioFrame(
                        content = it.clone(),
                        duration = reader.frameDuration,
                        timestamp = it.timestamp.microseconds,
                    )
                )
            }
        }
    }

private suspend fun filterReverse(reader: AudioReader): List<AudioFrame> =
    filterAudioFrames(reader, "areverse")

private suspend fun filterChangeSpeed(
    reader: AudioReader,
    speedMultiplier: Double,
): List<AudioFrame> =
    filterAudioFrames(reader, "atempo=$speedMultiplier")
        .map {
            it.copy(
                duration = it.duration / speedMultiplier,
                timestamp = it.content.timestamp.microseconds,
            )
        }
