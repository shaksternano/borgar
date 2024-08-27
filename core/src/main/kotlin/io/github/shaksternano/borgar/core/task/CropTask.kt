package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageProcessor
import io.github.shaksternano.borgar.core.media.MediaProcessingConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessingConfig
import kotlinx.coroutines.flow.Flow
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min

class CropTask(
    topRatio: Double,
    bottomRatio: Double,
    leftRatio: Double,
    rightRatio: Double,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SimpleMediaProcessingConfig(
        processor = CropProcessor(
            leftRatio,
            topRatio,
            rightRatio,
            bottomRatio,
        ),
        outputName = "captioned"
    )
}

private class CropProcessor(
    private val leftRatio: Double,
    private val topRatio: Double,
    private val rightRatio: Double,
    private val bottomRatio: Double,
) : ImageProcessor<CropData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String
    ): CropData {
        val firstImage = firstFrame.content
        val width = firstImage.width
        val height = firstImage.height
        val x = min(width * leftRatio, width - 1.0).toInt()
        val y = min(height * topRatio, height - 1.0).toInt()
        val newWidth = max(width * (1 - leftRatio - rightRatio), 1.0).toInt()
        val newHeight = max(height * (1 - topRatio - bottomRatio), 1.0).toInt()
        return CropData(x, y, newWidth, newHeight)
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: CropData): BufferedImage =
        frame.content.getSubimage(constantData.x, constantData.y, constantData.width, constantData.height)
}

private class CropData(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)
