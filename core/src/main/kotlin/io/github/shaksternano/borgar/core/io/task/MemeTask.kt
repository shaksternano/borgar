package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.graphics.configureTextDrawQuality
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.graphics.drawable.OutlinedTextDrawable
import io.github.shaksternano.borgar.core.graphics.drawable.ParagraphCompositeDrawable
import io.github.shaksternano.borgar.core.graphics.fitFontHeight
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageProcessor
import io.github.shaksternano.borgar.core.media.MediaProcessConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessConfig
import io.github.shaksternano.borgar.core.media.graphics.TextAlignment
import io.github.shaksternano.borgar.core.util.splitWords
import kotlinx.coroutines.flow.Flow
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import kotlin.math.min

class MemeTask(
    topWords: String,
    bottomWords: String,
    nonTextParts: Map<String, Drawable>,
    maxFileSize: Long
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessConfig = SimpleMediaProcessConfig(
        processor = MemeProcessor(
            topWords,
            bottomWords,
            nonTextParts,
        ),
        outputName = "meme"
    )
}

private class MemeProcessor(
    topWords: String,
    bottomWords: String,
    private val nonTextParts: Map<String, Drawable>,
) : ImageProcessor<MemeData> {

    private val topWords: List<String> = topWords.splitWords()
    private val bottomWords: List<String> = bottomWords.splitWords()

    override suspend fun transformImage(frame: ImageFrame, constantData: MemeData): BufferedImage {
        val result = frame.content
        val graphics = result.createGraphics()
        graphics.configureTextDrawQuality()

        graphics.font = constantData.topFont
        constantData.topParagraph.draw(
            graphics,
            constantData.topParagraphX,
            constantData.topParagraphY,
            frame.timestamp
        )

        graphics.font = constantData.bottomFont
        constantData.bottomParagraph.draw(
            graphics,
            constantData.bottomParagraphX,
            constantData.bottomParagraphY,
            frame.timestamp
        )

        graphics.dispose()
        return result
    }

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

        val topParagraph = ParagraphCompositeDrawable.Builder(nonTextParts)
            .addWords(topWords) {
                createText(it)
            }
            .build(TextAlignment.CENTRE, textWidth)
        val bottomParagraph = ParagraphCompositeDrawable.Builder(nonTextParts)
            .addWords(bottomWords) {
                createText(it)
            }
            .build(TextAlignment.CENTRE, textWidth)

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
