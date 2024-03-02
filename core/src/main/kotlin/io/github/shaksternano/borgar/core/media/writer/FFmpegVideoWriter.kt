package io.github.shaksternano.borgar.core.media.writer

import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.image.BufferedImage
import java.nio.file.Path
import kotlin.time.Duration

private const val MAX_DIMENSION = 2000
private const val MAX_AUDIO_FRAME_RATE = 1000

class FFmpegVideoWriter(
    private val output: Path,
    private val outputFormat: String,
    private val audioChannels: Int,
    private val audioSampleRate: Int,
    private val audioBitrate: Int,
    private val maxFileSize: Long,
    private val maxDuration: Duration,
) : MediaWriter {

    private lateinit var recorder: FFmpegFrameRecorder
    private val converter: Java2DFrameConverter = Java2DFrameConverter()
    private var closed: Boolean = false

    override val isStatic: Boolean = false
    override val supportsAudio: Boolean = true

    override suspend fun writeImageFrame(frame: ImageFrame) {
        val image = frame.content
            .bound(MAX_DIMENSION)
            .convertType(BufferedImage.TYPE_3BYTE_BGR)
        if (!::recorder.isInitialized) {
            val fps = 1000000.0 / frame.duration.inWholeMicroseconds
            recorder = createFFmpegRecorder(
                output,
                outputFormat,
                image.width,
                image.height,
                fps,
                audioChannels,
                audioSampleRate,
                audioBitrate,
                maxFileSize,
                maxDuration,
            )
            withContext(Dispatchers.IO) {
                recorder.start()
            }
        }
        val converted = converter.convert(image)
        recorder.record(converted)
    }

    override suspend fun writeAudioFrame(frame: AudioFrame) {
        require(::recorder.isInitialized) {
            "Cannot write audio frame before image frame"
        }
        // Prevent errors from occurring when the frame rate is too high.
        if (recorder.frameRate <= MAX_AUDIO_FRAME_RATE) {
            withContext(Dispatchers.IO) {
                recorder.record(frame.content)
            }
        }
    }

    override suspend fun close() {
        if (closed) return
        closed = true
        closeAll(
            SuspendCloseable.fromBlocking {
                if (::recorder.isInitialized) {
                    recorder.close()
                }
            },
            SuspendCloseable(converter),
        )
    }

    private fun createFFmpegRecorder(
        path: Path,
        format: String,
        imageWidth: Int,
        imageHeight: Int,
        fps: Double,
        audioChannels: Int,
        audioSampleRate: Int,
        audioBitrate: Int,
        maxFileSize: Long,
        maxDuration: Duration,
    ): FFmpegFrameRecorder {
        var audioSampleRate1 = audioSampleRate
        var audioBitrate1 = audioBitrate
        var videoBitrate = calculateVideoBitrate(imageWidth, imageHeight, fps, 0.1)
        val totalBitrate = videoBitrate + audioBitrate1
        val estimatedFileSize = estimateFileSize(totalBitrate, maxDuration)
        if (maxFileSize in 1..<estimatedFileSize) {
            val reductionRatio = maxFileSize.toDouble() / estimatedFileSize
            videoBitrate = (videoBitrate * reductionRatio).toInt()
            audioBitrate1 = (audioBitrate1 * reductionRatio).toInt()
        }
        val recorder = FFmpegFrameRecorder(
            path.toFile(),
            imageWidth,
            imageHeight,
            audioChannels,
        )
        recorder.format = format
        recorder.isInterleaved = true
        if (format == "webm") {
            // VP9 takes too long to encode. In one case it was over 4x slower than VP8.
            recorder.videoCodec = avcodec.AV_CODEC_ID_VP8
            recorder.audioCodec = avcodec.AV_CODEC_ID_OPUS
            audioSampleRate1 = getWebmSampleRate(audioSampleRate1)
        } else {
            recorder.videoCodec = avcodec.AV_CODEC_ID_H264
            recorder.audioCodec = avcodec.AV_CODEC_ID_AAC
            /*
            Decrease "startup" latency in FFMPEG
            (see: https://trac.ffmpeg.org/wiki/StreamingGuide).
             */
            recorder.setVideoOption("tune", "zerolatency")
        }

        /*
        Tradeoff between quality and encode speed.
        Possible values are: ultrafast, superfast, veryfast, faster, fast, medium, slow, slower, veryslow.
        Ultrafast offers us the least amount of compression
        (lower encoder CPU) at the cost of a larger stream size.
        At the other end, veryslow provides the best compression
        (high encoder CPU) while lowering the stream size
        (see: https://trac.ffmpeg.org/wiki/Encode/H.264).
         */
        recorder.setVideoOption("preset", "ultrafast")
        recorder.frameRate = fps
        recorder.videoBitrate = videoBitrate
        /*
        Key frame interval, in our case every 2 seconds -> fps * 2
        (GOP length)
         */
        recorder.gopSize = (fps * 2).toInt()

        // Highest quality
        recorder.audioQuality = 0.0
        recorder.sampleRate = audioSampleRate1
        recorder.audioBitrate = audioBitrate1
        return recorder
    }

    private fun getWebmSampleRate(sampleRate: Int): Int = when {
        sampleRate > 24000 -> 48000
        sampleRate > 16000 -> 24000
        sampleRate > 12000 -> 16000
        sampleRate > 8000 -> 12000
        else -> 8000
    }

    @Suppress("SameParameterValue")
    private fun calculateVideoBitrate(width: Int, height: Int, fps: Double, bitsPerPixel: Double): Int =
        (width * height * fps * bitsPerPixel).toInt()

    private fun estimateFileSize(bitrate: Int, duration: Duration): Long =
        bitrate * duration.inWholeSeconds / 8

    object Factory : MediaWriterFactory {
        // Default media writer
        override val supportedFormats: Set<String> = setOf()

        override suspend fun create(
            output: Path,
            outputFormat: String,
            loopCount: Int,
            audioChannels: Int,
            audioSampleRate: Int,
            audioBitrate: Int,
            maxFileSize: Long,
            maxDuration: Duration
        ): MediaWriter = FFmpegVideoWriter(
            output,
            outputFormat,
            audioChannels,
            audioSampleRate,
            audioBitrate,
            maxFileSize,
            maxDuration,
        )
    }
}
