package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.FrameInfo
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import io.github.shaksternano.borgar.core.media.findIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream
import kotlin.io.path.deleteIfExists
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class WebPImageReader(
    private val input: Path,
    private val imageInput: ImageInputStream,
    private val reader: javax.imageio.ImageReader,
    override val frameCount: Int,
    private val deleteInputOnClose: Boolean,
) : BaseImageReader() {

    override val frameRate: Double
    override val duration: Duration
    override val frameDuration: Duration
    override val width: Int
    override val height: Int
    override val loopCount: Int = 0
    private val frameInfos: List<FrameInfo>

    init {
        val webPReaderClass = Class.forName("com.twelvemonkeys.imageio.plugins.webp.WebPImageReader")
        val animationFrameClass = Class.forName("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame")

        require(webPReaderClass.isInstance(reader)) { "No WebP reader found" }
        reader.input = imageInput

        val framesField = webPReaderClass.getDeclaredField("frames")
        val durationField = animationFrameClass.getDeclaredField("duration")
        framesField.setAccessible(true)
        durationField.setAccessible(true)

        val animationFrames = framesField[reader] as List<*>
        frameInfos = mutableListOf()
        var shortestDuration = Duration.INFINITE
        duration = animationFrames.fold(Duration.ZERO) { total, animationFrame ->
            val frameDuration = (durationField[animationFrame] as Int).milliseconds
            if (frameDuration < shortestDuration) shortestDuration = frameDuration
            frameInfos.add(
                FrameInfo(
                    frameDuration,
                    total,
                )
            )
            total + frameDuration
        }
        if (frameInfos.isEmpty()) {
            frameInfos.add(FrameInfo(1.milliseconds, Duration.ZERO))
        }
        frameDuration = shortestDuration
        frameRate = 1000.0 / frameDuration.inWholeMilliseconds
        width = reader.getWidth(0)
        height = reader.getHeight(0)
    }

    override suspend fun readFrame(timestamp: Duration): ImageFrame {
        val circularTimestamp = (timestamp.inWholeMilliseconds % max(duration.inWholeMilliseconds, 1)).milliseconds
        val index = findIndex(circularTimestamp, frameInfos.map(FrameInfo::duration))
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

    private suspend fun read(index: Int): BufferedImage {
        val image = withContext(Dispatchers.IO) {
            reader.read(index)
        }
        // Remove alpha as sometimes frames are completely transparent.
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val rgb = image.getRGB(x, y)
                val alpha = rgb shr 24 and 0xFF
                if (alpha == 0 && rgb != 0 && rgb != 0xFFFFFF) {
                    val noAlpha = rgb or -0x1000000
                    image.setRGB(x, y, noAlpha)
                }
            }
        }
        return image
    }

    override suspend fun close() = closeAll(
        SuspendCloseable(reader::dispose),
        SuspendCloseable.fromBlocking(imageInput),
        SuspendCloseable.fromBlocking {
            if (deleteInputOnClose) {
                input.deleteIfExists()
            }
        }
    )

    object Factory : ImageReaderFactory {
        override val supportedFormats: Set<String> = setOf("webp")

        override suspend fun create(input: DataSource): ImageReader {
            val isTempFile = input.path == null
            val path = input.getOrWriteFile().path
            return withContext(Dispatchers.IO) {
                val imageInput = ImageIO.createImageInputStream(path.toFile())
                val readers = ImageIO.getImageReaders(imageInput)
                require(readers.hasNext()) { "No WebP reader found" }
                val reader = readers.next()
                val size = reader.getNumImages(true)
                WebPImageReader(
                    path,
                    imageInput,
                    reader,
                    size,
                    isTempFile,
                )
            }
        }
    }
}
