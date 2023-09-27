package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.CloseableIterator
import io.github.shaksternano.borgar.core.collect.CloseableSpliterator
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import io.github.shaksternano.borgar.core.media.convertType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.function.Consumer
import javax.imageio.ImageIO
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class JavaxImageReader(
    image: BufferedImage,
) : BaseImageReader() {

    private val frame: ImageFrame = run {
        // For some reason some images have a greyscale type, even though they have color
        val converted = image.convertType(BufferedImage.TYPE_INT_ARGB)
        ImageFrame(converted, 1.milliseconds, Duration.ZERO)
    }
    private val frameDeferred: Deferred<ImageFrame> = CompletableDeferred(frame)

    override val size: Int = 1
    override val frameRate: Double = 1.0
    override val duration: Duration = 1.milliseconds
    override val frameDuration: Duration = 1.milliseconds
    override val width: Int = frame.content.width
    override val height: Int = frame.content.height
    override val loopCount: Int = 0

    override suspend fun readFrame(timestamp: Duration): ImageFrame = frame

    override fun createReversed(): ImageReader = this

    override fun iterator(): CloseableIterator<Deferred<ImageFrame>> = CloseableIterator.singleton(frameDeferred)

    override fun forEach(action: Consumer<in Deferred<ImageFrame>>) = action.accept(frameDeferred)

    override fun spliterator(): CloseableSpliterator<Deferred<ImageFrame>> =
        CloseableSpliterator.singleton(frameDeferred)

    override fun close() = Unit

    object Factory : ImageReaderFactory {
        override val supportedFormats: Set<String> = setOf(
            "bmp",
            "jpeg",
            "jpg",
            "wbmp",
            "png",
            "gif",
            "tif",
            "tiff",
        )

        override suspend fun create(input: DataSource): ImageReader = input.newStream().use {
            val image = withContext(Dispatchers.IO) {
                ImageIO.read(it) ?: throw IOException("Failed to read image")
            }
            JavaxImageReader(image)
        }
    }
}
