package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.graphics.ContentPosition
import io.github.shaksternano.borgar.core.graphics.OverlayData
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.*
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.reader.ZippedImageReader
import io.github.shaksternano.borgar.core.media.reader.transform
import io.github.shaksternano.borgar.core.media.template.Template
import kotlinx.coroutines.flow.Flow
import java.awt.Color
import java.awt.Shape
import java.awt.image.BufferedImage

class TemplateTask(
    template: Template,
    text: String?,
    nonTextParts: Map<String, Drawable>,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val requireInput: Boolean = text.isNullOrBlank()
    override val suppliedInput: DataSource? =
        if (requireInput) null
        else template.media
    override val config: MediaProcessingConfig = TemplateConfig(template, text, nonTextParts)
}

private data class TemplateConfig(
    private val template: Template,
    private val text: String?,
    private val nonTextParts: Map<String, Drawable>,
    private val afterProcessor: ImageProcessor<*>? = null,
) : MediaProcessingConfig {

    override val outputName: String = template.resultName

    override suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader =
        if (text.isNullOrBlank()) {
            val templateReader = template.getImageReader()
            val zipped = ZippedImageReader(imageReader, templateReader)
            val processor = TemplateImageContentProcessor(template) then afterProcessor
            zipped.transform(processor, outputFormat)
        } else {
            val processor = TemplateTextContentProcessor(text, nonTextParts, template) then afterProcessor
            imageReader.transform(processor, outputFormat)
        }

    override fun transformOutputFormat(inputFormat: String): String =
        if (text.isNullOrBlank() && isStaticOnly(inputFormat) && !isStaticOnly(template.format)) {
            template.format
        } else if (template.forceTransparency) {
            equivalentTransparentFormat(inputFormat)
        } else {
            inputFormat
        }

    override fun then(after: MediaProcessingConfig): MediaProcessingConfig {
        return if (after is SimpleMediaProcessingConfig) {
            copy(afterProcessor = afterProcessor then after.processor)
        } else {
            super.then(after)
        }
    }
}

private class TemplateImageContentProcessor(
    private val template: Template,
) : ImageProcessor<ImageContentData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String,
    ): ImageContentData {
        val contentImage = firstFrame.content as DualBufferedImage
        val templateImage = contentImage.second

        val width = template.imageContentWidth
        val height = template.imageContentHeight
        val transformedContentImage = contentImage
            .resize(width, height)
            .rotate(template.contentRotationRadians)

        val transformedWidth = transformedContentImage.width
        val transformedHeight = transformedContentImage.height

        val contentImageX = template.imageContentX + ((template.imageContentWidth - transformedWidth) / 2)
        val contentImageY = when (template.imageContentPosition) {
            ContentPosition.TOP -> template.imageContentY
            ContentPosition.CENTRE -> template.imageContentY + ((template.imageContentHeight - transformedHeight) / 2)
            ContentPosition.BOTTOM -> template.imageContentY + (template.imageContentHeight - transformedHeight)
        }

        val fill = template.fill
            ?:
            if (transformedContentImage.colorModel.hasAlpha()) null
            else Color.WHITE

        val resultType = if (template.forceTransparency) {
            BufferedImage.TYPE_INT_ARGB
        } else {
            contentImage.typeNoCustom
        }
        val overlayData = getOverlayData(
            templateImage,
            contentImage,
            contentImageX,
            contentImageY,
            false,
            resultType,
        )

        return ImageContentData(
            overlayData,
            width,
            height,
            template.isBackground,
            template.getContentClip(),
            fill,
        )
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: ImageContentData): BufferedImage {
        val contentImage = frame.content as DualBufferedImage
        val templateImage = contentImage.second

        val transformedContentImage = contentImage
            .resize(constantData.contentImageTargetWidth, constantData.contentImageTargetHeight)
            .rotate(template.contentRotationRadians)
        return overlay(
            templateImage,
            transformedContentImage,
            constantData.overlayData,
            constantData.contentIsBackground,
            constantData.contentClip,
            constantData.fill
        )
    }
}

private class ImageContentData(
    val overlayData: OverlayData,
    val contentImageTargetWidth: Int,
    val contentImageTargetHeight: Int,
    val contentIsBackground: Boolean,
    val contentClip: Shape?,
    val fill: Color?,
)

private class TemplateTextContentProcessor(
    private val text: String,
    private val nonTextParts: Map<String, Drawable>,
    private val template: Template,
) : ImageProcessor<TextDrawData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String,
    ): TextDrawData =
        getTextDrawData(firstFrame.content, text, nonTextParts, template)

    override suspend fun transformImage(frame: ImageFrame, constantData: TextDrawData): BufferedImage =
        drawText(frame.content, constantData, frame.timestamp, template)
}
