package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.forEachNotNull
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.AudioReaderFactory
import io.github.shaksternano.borgar.core.media.frameAtTime
import kotlinx.coroutines.Dispatchers
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
    isTempFile,
    grabber,
) {

    override val audioBitrate: Int = if (audioChannels > 0 && audioBitrate == 0) {
        128000
    } else {
        audioBitrate
    }
    override val width: Int = 0
    override val height: Int = 0

    override suspend fun setTimestamp(timestamp: Duration, grabber: FFmpegFrameGrabber) = withContext(Dispatchers.IO) {
        grabber.setAudioTimestamp(timestamp.inWholeMicroseconds)
    }

    override suspend fun grabFrame(grabber: FFmpegFrameGrabber): Frame? = withContext(Dispatchers.IO) {
        grabber.grabSamples()
    }

    override fun convertFrame(frame: Frame): AudioFrame = AudioFrame(
        frame,
        frameDuration,
        frame.timestamp.microseconds,
    )

    override fun createReversed(): MediaReader<AudioFrame> = Reversed(this)

    override fun createChangedSpeed(speedMultiplier: Double): MediaReader<AudioFrame> =
        ChangedSpeedAudioReader(this, speedMultiplier)

    private class Reversed(
        private val reader: AudioReader,
    ) : ReversedAudioReader(reader) {

        private lateinit var reversedFrames: List<AudioFrame>

        private suspend fun init() {
            if (!::reversedFrames.isInitialized)
                reversedFrames = filterAudioFrames(reader, "areverse", audioChannels, frameDuration)
        }

        override suspend fun readFrame(timestamp: Duration): AudioFrame {
            init()
            return frameAtTime(timestamp, reversedFrames, duration)
        }

        override fun asFlow(): Flow<AudioFrame> = flow {
            init()
            reversedFrames.forEach {
                emit(it)
            }
        }

        override fun createChangedSpeed(speedMultiplier: Double): MediaReader<AudioFrame> =
            ChangedSpeedAudioReader(this, speedMultiplier)
    }

    private class ChangedSpeedAudioReader(
        reader: AudioReader,
        speedMultiplier: Double,
    ) : ChangedSpeedReader<AudioFrame>(
        reader,
        speedMultiplier,
    ) {

        private lateinit var changedSpeedFrames: List<AudioFrame>

        private suspend fun init() {
            if (!::changedSpeedFrames.isInitialized)
                changedSpeedFrames = filterAudioFrames(reader, "atempo=$speedMultiplier", audioChannels, frameDuration)
                    .map {
                        it.copy(
                            timestamp = it.timestamp / speedMultiplier,
                            duration = it.duration / speedMultiplier,
                        )
                    }
        }

        override suspend fun readFrame(timestamp: Duration): AudioFrame {
            init()
            return frameAtTime(timestamp, changedSpeedFrames, duration)
        }

        override fun asFlow(): Flow<AudioFrame> = flow {
            init()
            changedSpeedFrames.forEach {
                emit(it)
            }
        }
    }

    object Factory : AudioReaderFactory {
        // Default audio reader
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

private suspend fun filterAudioFrames(
    reader: AudioReader,
    filter: String,
    audioChannels: Int,
    frameDuration: Duration
): List<AudioFrame> =
    FFmpegFrameFilter(filter, audioChannels).use { frameFilter ->
        frameFilter.start()
        var sampleRate = -1
        reader.asFlow().collect {
            frameFilter.push(it.content)
            if (sampleRate < 0)
                sampleRate = it.content.sampleRate
        }
        // Without this, pull() will always return null
        frameFilter.push(null)
        buildList {
            forEachNotNull(frameFilter::pull) {
                it.sampleRate = sampleRate
                add(
                    AudioFrame(
                        it.clone(),
                        frameDuration,
                        it.timestamp.microseconds,
                    )
                )
            }
        }
    }
