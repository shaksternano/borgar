package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.io.use
import io.github.shaksternano.borgar.core.media.FrameInfo
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
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

    private val converter: Java2DFrameConverter = Java2DFrameConverter()
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
        return ImageFrame(
            converter.convert(frame),
            frameDuration,
            frame.timestamp.microseconds
        )
    }

    private fun isInvalidImageChannels(imageChannels: Int): Boolean =
        imageChannels != 1 && imageChannels != 3 && imageChannels != 4

    override fun createReversed(): MediaReader<ImageFrame> = Reversed(this)

    override suspend fun close() = closeAll(
        { super.close() },
        SuspendCloseable(converter),
    )

    private class Reversed(
        private val reader: FFmpegImageReader,
    ) : ReversedImageReader(reader) {

        private lateinit var reversedFrameInfo: List<FrameInfo>

        override suspend fun readFrame(timestamp: Duration): ImageFrame {
            val reversedTimestamp =
                duration - (timestamp.inWholeMicroseconds % duration.inWholeMicroseconds).microseconds
            val frame = reader.readFrame(reversedTimestamp)
            return frame.copy(
                timestamp = timestamp
            )
        }

        override fun asFlow(): Flow<ImageFrame> = flow {
            if (!::reversedFrameInfo.isInitialized) {
                reversedFrameInfo = createReversedFrameInfo()
            }
            val grabber = FFmpegFrameGrabber(reader.input.toFile())
            SuspendCloseable.fromBlocking(grabber).use {
                withContext(Dispatchers.IO) {
                    grabber.start()
                }
                reversedFrameInfo.forEach {
                    val frame = reader.readFrame(it.timestamp, grabber)
                    emit(frame)
                }
            }
        }

        private suspend fun createReversedFrameInfo(): List<FrameInfo> {
            val frameInfo = reader.asFlow().map { (_, duration, timestamp) ->
                FrameInfo(duration, timestamp)
            }.toList()
            return frameInfo.reversed()
        }
    }

    object Factory : ImageReaderFactory {
        // Default image reader
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
