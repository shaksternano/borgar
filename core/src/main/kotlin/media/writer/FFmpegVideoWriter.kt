package com.shakster.borgar.core.media.writer

import com.shakster.borgar.core.BotConfig
import com.shakster.borgar.core.io.IO_DISPATCHER
import com.shakster.borgar.core.media.AudioFrame
import com.shakster.borgar.core.media.ImageFrame
import com.shakster.borgar.core.media.MediaWriterFactory
import kotlinx.coroutines.withContext
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.image.BufferedImage
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.DurationUnit

private const val MAX_AUDIO_FRAME_RATE: Int = 1000

class FFmpegVideoWriter(
    private val output: Path,
    private val outputFormat: String,
    private val audioChannels: Int,
    private val audioSampleRate: Int,
    private val audioBitrate: Int,
    private val maxFileSize: Long,
    private val maxDuration: Duration,
) : MediaWriter {

    override val isStatic: Boolean = false
    override val supportsAudio: Boolean = true

    private lateinit var recorder: FFmpegFrameRecorder
    private var closed: Boolean = false

    override suspend fun writeImageFrame(frame: ImageFrame) {
        val image = frame.content
        if (!::recorder.isInitialized) {
            val fps = 1000000 / frame.duration.toDouble(DurationUnit.MICROSECONDS)
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
            withContext(IO_DISPATCHER) {
                recorder.start()
            }
        }
        Java2DFrameConverter().use { converter ->
            converter.convert(image).use { converted ->
                withContext(IO_DISPATCHER) {
                    recorder.record(converted)
                }
            }
        }
    }

    override suspend fun writeAudioFrame(frame: AudioFrame) {
        require(::recorder.isInitialized) {
            "Cannot write audio frame before image frame"
        }
        // Prevent errors from occurring when the frame rate is too high.
        if (recorder.frameRate <= MAX_AUDIO_FRAME_RATE) {
            withContext(IO_DISPATCHER) {
                recorder.record(frame.content)
            }
        }
    }

    override suspend fun close() {
        if (closed) return
        closed = true
        if (::recorder.isInitialized) {
            withContext(IO_DISPATCHER) {
                recorder.close()
            }
        }
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

        var customVideoEncoder = false
        if (format == "webm") {
            // VP9 takes too long to encode. In one case it was over 4x slower than VP8.
            val encoder = BotConfig.get().encoder.ffmpegWebmEncoder
            if (encoder.isBlank()) {
                recorder.videoCodec = avcodec.AV_CODEC_ID_VP8
            } else {
                recorder.videoCodecName = encoder
                customVideoEncoder = true
            }

            recorder.audioCodec = avcodec.AV_CODEC_ID_OPUS
            audioSampleRate1 = getWebmSampleRate(audioSampleRate1)
        } else {
            val encoder = BotConfig.get().encoder.ffmpegMp4Encoder
            if (encoder.isBlank()) {
                recorder.videoCodec = avcodec.AV_CODEC_ID_H264
            } else {
                recorder.videoCodecName = encoder
                customVideoEncoder = true
            }

            recorder.audioCodec = avcodec.AV_CODEC_ID_AAC

            if (!customVideoEncoder) {
                /*
                Decrease "startup" latency in FFMPEG
                (see: https://trac.ffmpeg.org/wiki/StreamingGuide).
                 */
                recorder.setVideoOption("tune", "zerolatency")
            }
        }

        if (!customVideoEncoder) {
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
        }
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

        /**
         * 720p
         */
        override val maxImageDimension: Int = 1280
        override val requiredImageType: Int = BufferedImage.TYPE_3BYTE_BGR

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
