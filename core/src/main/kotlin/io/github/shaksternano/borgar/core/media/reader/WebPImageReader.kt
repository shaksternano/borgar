package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.CloseableIterator
import io.github.shaksternano.borgar.core.collect.MappedList
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.FrameInfo
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import io.github.shaksternano.borgar.core.media.findIndex
import java.awt.image.BufferedImage
import java.io.Closeable
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream
import kotlin.io.path.deleteIfExists
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class WebPImageReader(
    private val input: Path,
    private val deleteInputOnClose: Boolean = false,
) : BaseImageReader() {

    override val size: Int
    override var frameRate: Double
    override var duration: Duration
    override var frameDuration: Duration
    override var width: Int
    override var height: Int
    override val loopCount: Int = 0
    private val imageInput: ImageInputStream
    private val reader: javax.imageio.ImageReader
    private val frameInfos: List<FrameInfo>

    init {
        imageInput = ImageIO.createImageInputStream(input.toFile())
        val readers = ImageIO.getImageReaders(imageInput)
        require(readers.hasNext()) { "No WebP reader found" }
        reader = readers.next()

        val webPReaderClass = Class.forName("com.twelvemonkeys.imageio.plugins.webp.WebPImageReader")
        val animationFrameClass = Class.forName("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame")

        require(webPReaderClass.isInstance(reader)) { "No WebP reader found" }
        reader.input = imageInput
        size = reader.getNumImages(true)

        val framesField = webPReaderClass.getDeclaredField("frames")
        val durationField = animationFrameClass.getDeclaredField("duration")
        framesField.setAccessible(true)
        durationField.setAccessible(true)

        val animationFrames = framesField[reader] as List<*>
        frameInfos = mutableListOf()
        duration = animationFrames.fold(Duration.ZERO) { total, animationFrame ->
            val frameDuration = (durationField[animationFrame] as Int).milliseconds
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
        frameDuration = duration / size
        frameRate = 1000.0 / frameDuration.inWholeMilliseconds
        width = reader.getWidth(0)
        height = reader.getHeight(0)
    }

    override fun readFrame(timestamp: Duration): ImageFrame {
        val circularTimestamp = (timestamp.inWholeMilliseconds % max(duration.inWholeMilliseconds, 1)).milliseconds
        val index = findIndex(circularTimestamp, MappedList(frameInfos, FrameInfo::duration))
        return ImageFrame(
            read(index),
            frameInfos[index].duration,
            circularTimestamp,
        )
    }

    private fun read(index: Int): BufferedImage {
        val image = reader.read(index)
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

    override fun createReversed(): ImageReader = Reversed(this)

    override fun iterator(): CloseableIterator<ImageFrame> = WebpIterator(this)

    override fun close() = closeAll(
        Closeable(reader::dispose),
        imageInput,
        Closeable { if (deleteInputOnClose) input.deleteIfExists() },
    )

    private data class IndexedFrameInfo(
        val duration: Duration,
        val timestamp: Duration,
        val index: Int,
    )

    private class Reversed(
        private val reader: WebPImageReader,
    ) : ReversedImageReader(reader) {

        private val reversedFrameInfo: List<IndexedFrameInfo> = buildList {
            reader.frameInfos.fold(Duration.ZERO) { timestamp, frameInfo ->
                add(
                    IndexedFrameInfo(
                        frameInfo.duration,
                        timestamp,
                        reversed.size,
                    )
                )
                timestamp + frameInfo.duration
            }
        }

        override fun readFrame(timestamp: Duration): ImageFrame {
            val circularTimestamp = (timestamp.inWholeMilliseconds % max(duration.inWholeMilliseconds, 1)).milliseconds
            val index = findIndex(
                circularTimestamp,
                MappedList(
                    reversedFrameInfo,
                    IndexedFrameInfo::timestamp
                ),
            )
            val frameInfo = reversedFrameInfo[index]
            return ImageFrame(
                reader.read(frameInfo.index),
                frameInfo.duration,
                circularTimestamp,
            )
        }

        override fun iterator(): CloseableIterator<ImageFrame> = ReversedIterator(this)

        private class ReversedIterator(
            private val reversed: Reversed,
        ) : CloseableIterator<ImageFrame> {

            private val iterator: Iterator<IndexedFrameInfo> = reversed.reversedFrameInfo.iterator()

            override fun hasNext(): Boolean = iterator.hasNext()

            override fun next(): ImageFrame {
                val frameInfo = iterator.next()
                val image = reversed.reader.read(frameInfo.index)
                return ImageFrame(
                    image,
                    frameInfo.duration,
                    frameInfo.timestamp,
                )
            }

            override fun close() = Unit
        }
    }

    private class WebpIterator(
        private val reader: WebPImageReader,
    ) : CloseableIterator<ImageFrame> {

        private var index: Int = 0
        private var timestamp: Duration = Duration.ZERO

        override fun hasNext(): Boolean = index < reader.size

        override fun next(): ImageFrame {
            val duration = reader.frameInfos[index].duration
            val frame = ImageFrame(
                reader.read(index),
                duration,
                timestamp,
            )
            index++
            timestamp += duration
            return frame
        }

        override fun close() = Unit
    }

    object Factory : ImageReaderFactory {
        override val supportedFormats: Set<String> = setOf("webp")

        override suspend fun create(input: DataSource): ImageReader {
            val isTempFile = input.path == null
            return WebPImageReader(input.getOrWriteFile().path, isTempFile)
        }
    }
}
