package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Java2DFrameConverter
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds

class FFmpegImageReader(
    input: Path,
    isTempFile: Boolean,
    grabber: FFmpegFrameGrabber,
    override val frameCount: Int,
    override val frameRate: Double,
    override val duration: Duration,
    override val frameDuration: Duration,
    override val width: Int,
    override val height: Int,
) : FFmpegMediaReader<ImageFrame>(
    input,
    isTempFile,
    grabber,
) {

    override val audioChannels: Int = 0
    override val audioSampleRate: Int = 0
    override val audioBitrate: Int = 0

    override suspend fun setTimestamp(timestamp: Duration, grabber: FFmpegFrameGrabber) = withContext(Dispatchers.IO) {
        grabber.setVideoTimestamp(timestamp.inWholeMicroseconds)
    }

    override suspend fun grabFrame(grabber: FFmpegFrameGrabber): Frame? = withContext(Dispatchers.IO) {
        grabber.grabImage()
    }

    override fun convertFrame(frame: Frame): ImageFrame {
        if (isInvalidImageChannels(frame.imageChannels)) {
            frame.imageChannels = 3
        }
        /*
         * Sharing one frame converter instance for every
         * frame conversion can sometimes produce corrupted
         * frames when reading too quickly. Instead, a new
         * instance is created for every frame.
         */
        val image = Java2DFrameConverter().use {
            it.convert(frame)
        }
        return ImageFrame(
            image,
            frameDuration,
            frame.timestamp.microseconds,
        )
    }

    private fun isInvalidImageChannels(imageChannels: Int): Boolean =
        imageChannels != 1 && imageChannels != 3 && imageChannels != 4

    override suspend fun close() =
        super.close()

    object Factory : ImageReaderFactory {

        // This is the default image reader factory
        override val supportedFormats: Set<String> = setOf()

        override suspend fun create(input: DataSource): ImageReader =
            createReader(input) { path, isTempFile, grabber, frameCount, frameRate, frameDuration ->
                FFmpegImageReader(
                    path,
                    isTempFile,
                    grabber,
                    frameCount,
                    frameRate,
                    grabber.timestamp.microseconds,
                    frameDuration,
                    grabber.imageWidth,
                    grabber.imageHeight,
                )
            }
    }
}
