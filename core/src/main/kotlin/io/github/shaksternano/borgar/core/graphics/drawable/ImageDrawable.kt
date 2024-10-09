package io.github.shaksternano.borgar.core.graphics.drawable

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.fileFormat
import io.github.shaksternano.borgar.core.media.createImageReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.reader.firstContent
import io.github.shaksternano.borgar.core.media.reader.readContent
import io.github.shaksternano.borgar.core.media.resizeHeight
import io.github.shaksternano.borgar.core.util.hash
import io.github.shaksternano.borgar.core.util.kClass
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.time.Duration

class ImageDrawable internal constructor(
    private val dataSource: DataSource,
    private val reader: ImageReader,
    private val firstFrame: BufferedImage,
    private val width: Int = firstFrame.width,
    private val height: Int = firstFrame.height,
) : Drawable {

    override suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration) {
        val image = reader.readContent(timestamp).resizeHeight(height)
        graphics.drawImage(image, x, y, null)
    }

    override suspend fun getWidth(graphics: Graphics2D): Int = width

    override suspend fun getHeight(graphics: Graphics2D): Int = height

    override fun resizeToHeight(height: Int): Drawable {
        return if (height == this.height) {
            this
        } else {
            val resizedImage = firstFrame.resizeHeight(height)
            ImageDrawable(
                dataSource,
                reader,
                firstFrame,
                resizedImage.width,
                height,
            )
        }
    }

    override suspend fun close() = reader.close()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false

        other as ImageDrawable

        if (dataSource != other.dataSource) return false
        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int = hash(
        dataSource,
        width,
        height,
    )

    override fun toString(): String {
        return "ImageDrawable(dataSource=$dataSource" +
            ", reader=$reader" +
            ", firstFrame=$firstFrame" +
            ", width=$width" +
            ", height=$height)"
    }
}

suspend fun ImageDrawable(
    input: DataSource,
): ImageDrawable {
    val reader = createImageReader(input, input.fileFormat())
    return ImageDrawable(
        input,
        reader,
        reader.firstContent(),
    )
}
