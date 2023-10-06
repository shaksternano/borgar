package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.AudioReaderFactory
import io.github.shaksternano.borgar.core.media.frameAtTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    override suspend fun setTimestamp(timestamp: Duration, grabber: FFmpegFrameGrabber) =
        grabber.setAudioTimestamp(timestamp.inWholeMicroseconds)

    override suspend fun grabFrame(grabber: FFmpegFrameGrabber): Frame? =
        grabber.grabSamples()

    override fun convertFrame(frame: Frame): AudioFrame = AudioFrame(
        frame,
        frameDuration,
        frame.timestamp.microseconds,
    )

    override fun createReversed(): MediaReader<AudioFrame> = Reversed(this)

    private class Reversed(
        private val reader: FFmpegAudioReader,
    ) : ReversedAudioReader(reader) {

        private lateinit var reversedFrames: List<AudioFrame>

        override suspend fun readFrame(timestamp: Duration): AudioFrame {
            createReversedFrames()
            return frameAtTime(timestamp, reversedFrames, duration)
        }

        override fun asFlow(): Flow<AudioFrame> = flow {
            createReversedFrames()
            reversedFrames.forEach {
                emit(it)
            }
        }

        private suspend fun createReversedFrames() {
            if (::reversedFrames.isInitialized) return
            FFmpegFrameFilter("areverse", audioChannels).use { reverseFilter ->
                reverseFilter.start()
                var sampleRate = -1
                reader.asFlow().collect {
                    reverseFilter.push(it.content)
                    if (sampleRate < 0) sampleRate = it.content.sampleRate
                }
                val reversedFrames = mutableListOf<AudioFrame>()
                var reversedFrame: Frame
                while (reverseFilter.pull().also { reversedFrame = it } != null) {
                    reversedFrame.sampleRate = sampleRate
                    reversedFrames.add(
                        AudioFrame(
                            reversedFrame.clone(),
                            frameDuration,
                            reversedFrame.timestamp.microseconds,
                        )
                    )
                }
                this.reversedFrames = reversedFrames
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
