package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.graphics.Position
import io.github.shaksternano.borgar.core.graphics.TextAlignment
import io.github.shaksternano.borgar.core.graphics.configureTextDrawQuality
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.graphics.drawable.ParagraphDrawable
import io.github.shaksternano.borgar.core.graphics.drawable.draw
import io.github.shaksternano.borgar.core.graphics.fillRect
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.*
import kotlinx.coroutines.flow.Flow
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.round

class DemotivateTask(
    text: String,
    subText: String,
    nonTextParts: Map<String, Drawable>,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SimpleMediaProcessingConfig(
        processor = DemotivateProcessor(
            text,
            subText,
            nonTextParts,
        ),
        outputName = "demotivated"
    )
}

private class DemotivateProcessor(
    private val text: String,
    private val subText: String,
    private val nonTextParts: Map<String, Drawable>,
) : ImageProcessor<DemotivateData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String
    ): DemotivateData {
        val firstImage = firstFrame.content
        val imageWidth = firstImage.width
        val imageHeight = firstImage.height
        val contentAverageDimension = (imageWidth + imageHeight) / 2

        val demotivateImagePadding = (contentAverageDimension * 0.2).toInt()

        val graphics = firstImage.createGraphics()

        val font = Font("Times", Font.PLAIN, contentAverageDimension / 6)
        val subFont = font.deriveFont(font.size / 3F)
        graphics.font = font
        graphics.configureTextDrawQuality()

        val textAlignment = TextAlignment.CENTRE
        val paragraph = ParagraphDrawable(
            text,
            nonTextParts,
            textAlignment,
            imageWidth,
        )
        val paragraphHeight = paragraph.getHeight(graphics)

        val subParagraph = ParagraphDrawable(
            subText,
            nonTextParts,
            textAlignment,
            imageWidth,
        )
        graphics.font = subFont
        val subParagraphHeight = subParagraph.getHeight(graphics)
        val mainToSubParagraphSpacing = subParagraphHeight / 4

        graphics.dispose()

        val demotivateWidth = imageWidth + (demotivateImagePadding * 2)
        val demotivateHeight =
            imageHeight + (demotivateImagePadding * 2) + paragraphHeight + mainToSubParagraphSpacing + subParagraphHeight

        val lineDiameter =
            max(round(contentAverageDimension * 0.005), 1.0).toInt()
        val lineImageSpacing = lineDiameter * 3

        val paragraphPosition = Position(
            demotivateImagePadding,
            demotivateImagePadding + imageHeight + (demotivateImagePadding / 2)
        )
        val subParagraphPosition = Position(
            demotivateImagePadding,
            demotivateImagePadding + imageHeight + (demotivateImagePadding / 2) + paragraphHeight + mainToSubParagraphSpacing
        )

        val topBorder = Rectangle(
            demotivateImagePadding - (lineDiameter + lineImageSpacing),
            demotivateImagePadding - (lineDiameter + lineImageSpacing),
            imageWidth + (lineDiameter * 2) + (lineImageSpacing * 2),
            lineDiameter
        )
        val bottomBorder = Rectangle(
            demotivateImagePadding - (lineDiameter + lineImageSpacing),
            demotivateImagePadding + imageHeight + lineImageSpacing,
            imageWidth + (lineDiameter * 2) + (lineImageSpacing * 2),
            lineDiameter
        )
        val leftBorder = Rectangle(
            demotivateImagePadding - (lineDiameter + lineImageSpacing),
            demotivateImagePadding - (lineDiameter + lineImageSpacing),
            lineDiameter,
            imageHeight + (lineDiameter * 2) + (lineImageSpacing * 2)
        )
        val rightBorder = Rectangle(
            demotivateImagePadding + imageWidth + lineImageSpacing,
            demotivateImagePadding - (lineDiameter + lineImageSpacing),
            lineDiameter,
            imageHeight + (lineDiameter * 2) + (lineImageSpacing * 2)
        )

        return DemotivateData(
            demotivateWidth,
            demotivateHeight,
            demotivateImagePadding,
            font,
            paragraph,
            paragraphPosition,
            subFont,
            subParagraph,
            subParagraphPosition,
            topBorder,
            bottomBorder,
            leftBorder,
            rightBorder
        )
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: DemotivateData): BufferedImage {
        val image = frame.content
        val result = BufferedImage(constantData.width, constantData.height, image.typeNoCustom)
        val graphics = result.createGraphics()

        // Draw background
        graphics.color = Color.BLACK
        graphics.fillRect(0, 0, constantData.width, constantData.height)

        // Draw image
        graphics.drawImage(image, constantData.imagePadding, constantData.imagePadding, null)

        // Draw border
        graphics.color = Color.WHITE
        graphics.fillRect(constantData.topBorder)
        graphics.fillRect(constantData.bottomBorder)
        graphics.fillRect(constantData.leftBorder)
        graphics.fillRect(constantData.rightBorder)

        // Draw text
        graphics.font = constantData.font
        graphics.configureTextDrawQuality()
        constantData.paragraph.draw(
            graphics,
            constantData.paragraphPosition,
            frame.timestamp
        )
        graphics.font = constantData.subFont
        constantData.subParagraph.draw(
            graphics,
            constantData.subParagraphPosition,
            frame.timestamp
        )

        graphics.dispose()
        return result
    }

    override suspend fun close() = closeAll(nonTextParts.values)
}

private class DemotivateData(
    val width: Int,
    val height: Int,
    val imagePadding: Int,
    val font: Font,
    val paragraph: Drawable,
    val paragraphPosition: Position,
    val subFont: Font,
    val subParagraph: Drawable,
    val subParagraphPosition: Position,
    val topBorder: Rectangle,
    val bottomBorder: Rectangle,
    val leftBorder: Rectangle,
    val rightBorder: Rectangle,
)
