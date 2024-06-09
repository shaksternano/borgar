package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.graphics.TextAlignment
import io.github.shaksternano.borgar.core.graphics.configureTextDrawQuality
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.graphics.drawable.ParagraphDrawable
import io.github.shaksternano.borgar.core.graphics.fitFontWidth
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.*
import kotlinx.coroutines.flow.Flow
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage

class CaptionTask(
    caption: String,
    isCaption2: Boolean,
    isBottom: Boolean,
    nonTextParts: Map<String, Drawable>,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SimpleMediaProcessingConfig(
        processor = CaptionProcessor(
            caption,
            isCaption2,
            isBottom,
            nonTextParts,
        ),
        outputName = "captioned",
    )
}

private class CaptionProcessor(
    private val caption: String,
    private val isCaption2: Boolean,
    private val isBottom: Boolean,
    private val nonTextParts: Map<String, Drawable>,
) : ImageProcessor<CaptionData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String
    ): CaptionData {
        val firstImage = firstFrame.content
        val width = firstImage.width

        val fontName = if (isCaption2) "Helvetica Neue" else "Futura-CondensedExtraBold"
        val fontRatio = 0.1
        val paddingRatio = 0.04
        var font = Font(fontName, Font.PLAIN, (width * fontRatio).toInt())
        val padding = (width * paddingRatio).toInt()
        val graphics = firstImage.createGraphics()

        graphics.font = font
        graphics.configureTextDrawQuality()

        val maxWidth = width - padding * 2

        val textAlignment = if (isCaption2) {
            TextAlignment.LEFT
        } else {
            TextAlignment.CENTRE
        }

        val paragraph = ParagraphDrawable(
            caption,
            nonTextParts,
            textAlignment,
            maxWidth,
        )

        graphics.fitFontWidth(maxWidth, paragraph)
        font = graphics.font
        val fillHeight = paragraph.getHeight(graphics) + padding * 2
        graphics.dispose()
        return CaptionData(font, fillHeight, padding, paragraph)
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: CaptionData): BufferedImage {
        val image = frame.content
        val captionedImage = BufferedImage(
            image.width,
            image.height + constantData.fillHeight,
            image.typeNoCustom,
        )
        val graphics = captionedImage.createGraphics()
        graphics.configureTextDrawQuality()

        val imageY = if (isBottom) 0 else constantData.fillHeight
        val captionY = if (isBottom) image.height else 0

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
            frame.timestamp,
        )

        graphics.dispose()

        return captionedImage
    }

    override suspend fun close() = closeAll(nonTextParts.values)
}

private class CaptionData(
    val font: Font,
    val fillHeight: Int,
    val padding: Int,
    val paragraph: Drawable,
)
