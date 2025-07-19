package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.graphics.TextAlignment
import io.github.shaksternano.borgar.core.graphics.configureTextDrawQuality
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.graphics.drawable.OutlinedTextDrawable
import io.github.shaksternano.borgar.core.graphics.drawable.ParagraphDrawable
import io.github.shaksternano.borgar.core.graphics.fitFontHeight
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageProcessor
import io.github.shaksternano.borgar.core.media.MediaProcessingConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessingConfig
import kotlinx.coroutines.flow.Flow
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import kotlin.math.min

class MemeTask(
    topText: String,
    bottomText: String,
    nonTextParts: Map<String, Drawable>,
    maxFileSize: Long
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SimpleMediaProcessingConfig(
        processor = MemeProcessor(
            topText,
            bottomText,
            nonTextParts,
        ),
    )
}

private class MemeProcessor(
    private val topText: String,
    private val bottomText: String,
    private val nonTextParts: Map<String, Drawable>,
) : ImageProcessor<MemeData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String
    ): MemeData {
        val firstImage = firstFrame.content
        val imageWidth = firstImage.width
        val imageHeight = firstImage.height

        val smallestDimension = min(imageWidth, imageHeight)
        val padding = (smallestDimension * 0.04).toInt()

        val textWidth = imageWidth - (padding * 2)
        val textHeight = (imageHeight / 5) - (padding * 2)

        val bottomParagraphY = firstImage.height - textHeight - padding

        val topParagraph = ParagraphDrawable(
            topText,
            nonTextParts,
            TextAlignment.CENTRE,
            textWidth,
            ::createText,
        )
        val bottomParagraph = ParagraphDrawable(
            bottomText,
            nonTextParts,
            TextAlignment.CENTRE,
            textWidth,
            ::createText,
        )

        val font = Font("Impact", Font.BOLD, smallestDimension)

        val graphics = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
        graphics.configureTextDrawQuality()
        graphics.font = font
        graphics.fitFontHeight(textHeight, topParagraph)
        val topFont = graphics.font
        graphics.font = font
        graphics.fitFontHeight(textHeight, bottomParagraph)
        val bottomFont = graphics.font
        graphics.dispose()

        return MemeData(
            topParagraph,
            topFont,
            padding,
            padding,
            bottomParagraph,
            bottomFont,
            padding,
            bottomParagraphY
        )
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: MemeData): BufferedImage {
        val result = frame.content
        val graphics = result.createGraphics()
        graphics.configureTextDrawQuality()

        graphics.font = constantData.topFont
        constantData.topParagraph.draw(
            graphics,
            constantData.topParagraphX,
            constantData.topParagraphY,
            frame.timestamp,
        )

        graphics.font = constantData.bottomFont
        constantData.bottomParagraph.draw(
            graphics,
            constantData.bottomParagraphX,
            constantData.bottomParagraphY,
            frame.timestamp,
        )

        graphics.dispose()
        return result
    }

    private fun createText(word: String): Drawable =
        OutlinedTextDrawable(word, Color.WHITE, Color.BLACK, 0.15)

    override suspend fun close() = closeAll(nonTextParts.values)
}

private class MemeData(
    val topParagraph: Drawable,
    val topFont: Font,
    val topParagraphX: Int,
    val topParagraphY: Int,
    val bottomParagraph: Drawable,
    val bottomFont: Font,
    val bottomParagraphX: Int,
    val bottomParagraphY: Int
)
