package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.IO_DISPATCHER
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.FrameInfo
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import io.github.shaksternano.borgar.core.media.findIndex
import io.github.shaksternano.borgar.core.util.circular
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class WebPImageReader(
    private val imageInput: ImageInputStream,
    private val reader: javax.imageio.ImageReader,
    override val frameCount: Int,
) : BaseImageReader() {

    override val frameRate: Double
    override val duration: Duration
    override val frameDuration: Duration
    override val width: Int
    override val height: Int
    override val loopCount: Int = 0

    private val frameInfos: List<FrameInfo>
    private val mutex: Mutex = Mutex()

    init {
        val webPReaderClass = Class.forName("com.twelvemonkeys.imageio.plugins.webp.WebPImageReader")
        val animationFrameClass = Class.forName("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame")
        require(webPReaderClass.isInstance(reader)) { "No WebP reader found" }

        val framesField = webPReaderClass.getDeclaredField("frames")
        val durationField = animationFrameClass.getDeclaredField("duration")
        framesField.setAccessible(true)
        durationField.setAccessible(true)

        val animationFrames = framesField[reader] as List<*>
        var shortestDuration = Duration.INFINITE
        frameInfos = buildList {
            duration = animationFrames.fold(Duration.ZERO) { total, animationFrame ->
                val frameDuration = (durationField[animationFrame] as Int).milliseconds
                if (frameDuration < shortestDuration) {
                    shortestDuration = frameDuration
                }
                add(
                    FrameInfo(
                        frameDuration,
                        total,
                    )
                )
                total + frameDuration
            }
            if (isEmpty()) {
                add(FrameInfo(1.milliseconds, Duration.ZERO))
            }
        }
        frameDuration = shortestDuration
        frameRate = 1000.0 / frameDuration.inWholeMilliseconds
        width = reader.getWidth(0)
        height = reader.getHeight(0)
    }

    override suspend fun readFrame(timestamp: Duration): ImageFrame {
        val circularTimestamp = timestamp.circular(duration)
        val index = findIndex(circularTimestamp, frameInfos.map(FrameInfo::timestamp))
        return ImageFrame(
            read(index),
            frameInfos[index].duration,
            circularTimestamp,
        )
    }

    override fun asFlow(): Flow<ImageFrame> = flow {
        frameInfos.foldIndexed(Duration.ZERO) { i, timestamp, frameInfo ->
            val frame = ImageFrame(
                read(i),
                frameInfo.duration,
                timestamp,
            )
            emit(frame)
            timestamp + frameInfo.duration
        }
    }

    private suspend fun read(index: Int): BufferedImage = mutex.withLock {
        withContext(IO_DISPATCHER) {
            reader.read(index)
        }
    }

    override suspend fun close() = closeAll(
        SuspendCloseable(reader::dispose),
        SuspendCloseable.fromBlocking(imageInput),
    )

    object Factory : ImageReaderFactory {

        override val supportedFormats: Set<String> = setOf("webp")

        override suspend fun create(input: DataSource): ImageReader = withContext(IO_DISPATCHER) {
            val imageInput = ImageIO.createImageInputStream(input.path?.toFile() ?: input.newStream())
            val readers = ImageIO.getImageReaders(imageInput)
            require(readers.hasNext()) { "No WebP reader found" }
            val reader = readers.next()
            reader.input = imageInput
            val size = reader.getNumImages(true)
            WebPImageReader(
                imageInput,
                reader,
                size,
            )
        }
    }
}
