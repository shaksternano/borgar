package io.github.shaksternano.borgar.core.graphics.drawable

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.createImageReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.reader.firstContent
import io.github.shaksternano.borgar.core.media.reader.readContent
import io.github.shaksternano.borgar.core.media.resizeHeight
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.time.Duration

class ImageDrawable internal constructor(
    private val reader: ImageReader,
    private val firstFrame: BufferedImage,
    private val width: Int = firstFrame.width,
    private val height: Int = firstFrame.height,
) : Drawable {

    override suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration) {
        val image = reader.readContent(timestamp).resizeHeight(height)
        graphics.drawImage(image, x, y, null)
    }

    override fun getWidth(graphicsContext: Graphics2D): Int = width

    override fun getHeight(graphicsContext: Graphics2D): Int = height

    override fun resizeToHeight(height: Int): Drawable {
        return if (height == this.height) {
            this
        } else {
            val resizedImage = firstFrame.resizeHeight(height)
            ImageDrawable(
                reader,
                firstFrame,
                resizedImage.width,
                height,
            )
        }
    }

    override suspend fun close() = reader.close()
}

suspend fun ImageDrawable(
    input: DataSource,
    format: String,
): ImageDrawable {
    val reader = createImageReader(input, format)
    return ImageDrawable(
        reader,
        reader.firstContent(),
    )
}
