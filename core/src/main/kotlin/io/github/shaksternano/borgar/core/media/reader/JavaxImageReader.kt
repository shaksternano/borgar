package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.CloseableIterator
import io.github.shaksternano.borgar.core.collect.CloseableSpliterator
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import io.github.shaksternano.borgar.core.media.ImageUtil
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.function.Consumer
import javax.imageio.ImageIO

class JavaxImageReader(
    input: DataSource,
) : BaseImageReader() {

    private val frame: ImageFrame

    init {
        frame = input.newStreamBlocking().use {
            val image = ImageIO.read(it) ?: throw IOException("Failed to read image")
            // For some reason some images have a greyscale type, even though they have color
            val converted = ImageUtil.convertType(image, BufferedImage.TYPE_INT_ARGB)
            ImageFrame(converted, 1.0, 0.0)
        }
    }

    override val size: Int = 1
    override val frameRate: Double = 1.0
    override val duration: Double = 1.0
    override val frameDuration: Double = 1.0
    override val width: Int = frame.content.width
    override val height: Int = frame.content.height
    override val loopCount: Int = 0

    override fun readFrame(timestamp: Double): ImageFrame = frame

    override fun createReversed(): ImageReader = this

    override fun iterator(): CloseableIterator<ImageFrame> = CloseableIterator.singleton(frame)

    override fun forEach(action: Consumer<in ImageFrame>) = action.accept(frame)

    override fun spliterator(): CloseableSpliterator<ImageFrame> = CloseableSpliterator.singleton(frame)

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

        override fun create(input: DataSource): ImageReader = JavaxImageReader(input)
    }
}
