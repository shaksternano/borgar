package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.*
import io.github.shaksternano.borgar.core.media.graphics.GraphicsUtil
import io.github.shaksternano.borgar.core.media.graphics.TextAlignment
import io.github.shaksternano.borgar.core.media.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.media.graphics.drawable.ParagraphCompositeDrawable
import io.github.shaksternano.borgar.core.util.splitWords
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage

class CaptionTask(
    caption: String,
    isCaption2: Boolean,
    nonTextParts: Map<String, Drawable>,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessConfig = BasicMediaProcessConfig(
        processor = CaptionProcessor(
            caption,
            isCaption2,
            nonTextParts,
        ),
        outputName = "captioned"
    )
}

private class CaptionProcessor(
    caption: String,
    private val isCaption2: Boolean,
    private val nonTextParts: Map<String, Drawable>,
) : ImageProcessor<CaptionData> {

    private val words = caption.splitWords()

    override fun transformImage(frame: ImageFrame, constantData: CaptionData): BufferedImage {
        val image = frame.content
        val captionedImage = BufferedImage(
            image.width,
            image.height + constantData.fillHeight,
            image.typeNoCustom,
        )
        val graphics = captionedImage.createGraphics()
        ImageUtil.configureTextDrawQuality(graphics)

        val imageY = if (isCaption2) 0 else constantData.fillHeight
        val captionY = if (isCaption2) image.height else 0

        graphics.drawImage(image, 0, imageY, null)

        graphics.color = Color.WHITE
        graphics.fillRect(
            0,
            captionY,
            captionedImage.width,
            constantData.fillHeight,
        )

        graphics.font = constantData.font
        graphics.color = Color.BLACK
        constantData.paragraph.draw(
            graphics,
            constantData.padding,
            captionY + constantData.padding,
            frame.timestamp.inWholeMicroseconds
        )

        graphics.dispose()

        return captionedImage
    }

    override suspend fun constantData(image: BufferedImage): CaptionData {
        val width = image.width
        val height = image.height

        val averageDimension = (width + height) / 2

        val fontName = if (isCaption2) "Helvetica Neue" else "Futura-CondensedExtraBold"
        val fontRatio = (if (isCaption2) 9 else 7).toFloat()
        var font = Font(fontName, Font.PLAIN, (averageDimension / fontRatio).toInt())
        val padding = (averageDimension * 0.04f).toInt()
        val graphics = image.createGraphics()

        graphics.font = font
        ImageUtil.configureTextDrawQuality(graphics)

        val maxWidth = width - padding * 2

        val textAlignment = if (isCaption2) {
            TextAlignment.LEFT
        } else {
            TextAlignment.CENTRE
        }

        val paragraph = ParagraphCompositeDrawable.Builder(nonTextParts)
            .addWords(null, words)
            .build(textAlignment, maxWidth)

        GraphicsUtil.fontFitWidth(maxWidth, paragraph, graphics)
        font = graphics.font
        val fillHeight = paragraph.getHeight(graphics) + padding * 2
        graphics.dispose()
        return CaptionData(font, fillHeight, padding, paragraph)
    }

    override fun close() = closeAll(nonTextParts.values)
}

private class CaptionData(
    val font: Font,
    val fillHeight: Int,
    val padding: Int,
    val paragraph: Drawable,
)
