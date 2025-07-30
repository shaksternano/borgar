package com.shakster.borgar.core.task

import com.shakster.borgar.core.media.*
import kotlinx.coroutines.flow.Flow
import java.awt.Color
import java.awt.image.BufferedImage

class RotateTask(
    degrees: Double,
    backgroundColor: Color?,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = RotateConfig(degrees, backgroundColor)
}

private class RotateConfig(
    degrees: Double,
    backgroundColor: Color?,
) : SimpleMediaProcessingConfig(
    processor = RotateProcessor(degrees, backgroundColor),
) {

    override fun transformOutputFormat(inputFormat: String): String =
        equivalentTransparentFormat(inputFormat)
}

private class RotateProcessor(
    private val degrees: Double,
    private val backgroundColor: Color?,
) : ImageProcessor<RotateData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String
    ): RotateData {
        val firstImage = firstFrame.content
        val resultType = firstImage.supportedTransparentImageType(outputFormat)
        return RotateData(resultType)
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: RotateData): BufferedImage {
        val image = frame.content
        val radians = Math.toRadians(degrees)
        val resultType = constantData.resultImageType
        return image.rotate(radians, resultType, backgroundColor)
    }
}

@JvmInline
private value class RotateData(
    val resultImageType: Int,
)
