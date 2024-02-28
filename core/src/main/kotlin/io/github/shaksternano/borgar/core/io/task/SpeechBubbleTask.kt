package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.exception.FailedOperationException
import io.github.shaksternano.borgar.core.graphics.OverlayData
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.*
import io.github.shaksternano.borgar.core.media.reader.firstContent
import kotlinx.coroutines.flow.Flow
import java.awt.Color
import java.awt.image.BufferedImage

class SpeechBubbleTask(
    cutout: Boolean,
    flipped: Boolean,
    opaque: Boolean,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SimpleMediaProcessingConfig(
        SpeechBubbleProcessor(cutout, flipped, opaque),
        "speech_bubbled",
    )
}

private class SpeechBubbleProcessor(
    private val cutout: Boolean,
    private val flipped: Boolean,
    private val opaque: Boolean,
) : ImageProcessor<SpeechBubbleData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String
    ): SpeechBubbleData {
        val image = firstFrame.content
        val width = image.width
        val height = image.height

        val speechBubblePath =
            if (cutout) "media/overlay/speech_bubble_2_partial.png"
            else "media/overlay/speech_bubble_1_partial.png"
        val dataSource = DataSource.fromResource(speechBubblePath)
        val speechBubbleImage = createImageReader(dataSource).firstContent()
        val minDimension = 3
        if (width < minDimension) {
            throw FailedOperationException("Image width of $width pixels is too small!")
        } else {
            if (speechBubbleImage.height < speechBubbleImage.width) {
                val scaleRatio = width.toDouble() / speechBubbleImage.width
                val newHeight = (speechBubbleImage.height * scaleRatio).toInt()
                if (newHeight < minDimension) {
                    throw FailedOperationException("Image height of $height pixels is too small!")
                }
            }
        }

        val resizedSpeechBubble = speechBubbleImage.resizeWidth(width)
        val result = (
            if (cutout) resizedSpeechBubble
            else resizedSpeechBubble.fill(Color.WHITE)
            ).let {
                if (flipped) it.flipX()
                else it
            }
        val overlayData = getOverlayData(image, result, 0, -result.height, true)
        val cutoutColor =
            if (opaque) Color.WHITE.rgb
            else 0
        return SpeechBubbleData(
            result,
            overlayData,
            cutoutColor
        )
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: SpeechBubbleData): BufferedImage {
        val image = frame.content
        val speechBubble = constantData.speechBubble
        return if (cutout) {
            val cutoutColor = constantData.cutoutColor
            image.cutout(speechBubble, 0, 0, cutoutColor)
        } else {
            overlay(
                image,
                speechBubble,
                constantData.overlayData,
                false,
            )
        }
    }
}

private data class SpeechBubbleData(
    val speechBubble: BufferedImage,
    val overlayData: OverlayData,
    val cutoutColor: Int,
)
