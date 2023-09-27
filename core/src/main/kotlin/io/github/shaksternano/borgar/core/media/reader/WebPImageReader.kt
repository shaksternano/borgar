package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.AsyncCloseableIterator
import io.github.shaksternano.borgar.core.collect.AsyncIterator
import io.github.shaksternano.borgar.core.collect.CloseableIterator
import io.github.shaksternano.borgar.core.collect.MappedList
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.FrameInfo
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import io.github.shaksternano.borgar.core.media.findIndex
import kotlinx.coroutines.*
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
    private val imageInput: ImageInputStream,
    private val reader: javax.imageio.ImageReader,
    override val size: Int,
    private val deleteInputOnClose: Boolean = false,
) : BaseImageReader() {

    override var frameRate: Double
    override var duration: Duration
    override var frameDuration: Duration
    override var width: Int
    override var height: Int
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

    override suspend fun readFrame(timestamp: Duration): ImageFrame {
        val circularTimestamp = (timestamp.inWholeMilliseconds % max(duration.inWholeMilliseconds, 1)).milliseconds
        val index = findIndex(circularTimestamp, MappedList(frameInfos, FrameInfo::duration))
        return ImageFrame(
            read(index),
            frameInfos[index].duration,
            circularTimestamp,
        )
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

    override fun createReversed(): ImageReader = Reversed(this)

    override fun iterator(): CloseableIterator<Deferred<ImageFrame>> = WebpIterator(this)

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

        override suspend fun readFrame(timestamp: Duration): ImageFrame {
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

        override fun iterator(): CloseableIterator<Deferred<ImageFrame>> = ReversedIterator(this)

        private class ReversedIterator(
            private val reversed: Reversed,
        ) : AsyncIterator<ImageFrame>(CoroutineScope(Dispatchers.IO)), CloseableIterator<Deferred<ImageFrame>> {

            private val iterator: Iterator<IndexedFrameInfo> = reversed.reversedFrameInfo.iterator()

            override fun hasNext(): Boolean = iterator.hasNext()

            override fun next(): Deferred<ImageFrame> {
                val frameInfo = iterator.next()
                return async {
                    val image = reversed.reader.read(frameInfo.index)
                    ImageFrame(
                        image,
                        frameInfo.duration,
                        frameInfo.timestamp,
                    )
                }
            }

            override fun close() = Unit
        }
    }

    private class WebpIterator(
        private val reader: WebPImageReader,
    ) : AsyncCloseableIterator<ImageFrame>(CoroutineScope(Dispatchers.IO)) {

        private var index: Int = 0
        private var timestamp: Duration = Duration.ZERO

        override fun hasNext(): Boolean = index < reader.size

        override fun next(): Deferred<ImageFrame> {
            val duration = reader.frameInfos[index].duration
            return async {
                val frame = ImageFrame(
                    reader.read(index),
                    duration,
                    timestamp,
                )
                index++
                timestamp += duration
                frame
            }
        }

        override fun close() = Unit
    }

    object Factory : ImageReaderFactory {
        override val supportedFormats: Set<String> = setOf("webp")

        override suspend fun create(input: DataSource): ImageReader = withContext(Dispatchers.IO) {
            val isTempFile = input.path == null
            val path = input.getOrWriteFile().path
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
