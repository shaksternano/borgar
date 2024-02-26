package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.media.*
import kotlinx.coroutines.flow.Flow
import java.awt.Color
import java.awt.image.BufferedImage

class RotateTask(
    degrees: Double,
    backgroundColor: Color?,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessConfig = RotateConfig(degrees, backgroundColor)
}

private class RotateConfig(
    degrees: Double,
    backgroundColor: Color?,
) : MediaProcessConfig {

    override val processor: ImageProcessor<out Any> = RotateProcessor(degrees, backgroundColor)

    override val outputName: String = "rotated"

    override fun transformOutputFormat(inputFormat: String): String =
        equivalentTransparentFormat(inputFormat)
}

private class RotateProcessor(
    private val degrees: Double,
    private val backgroundColor: Color?,
) : ImageProcessor<RotateData> {

    override suspend fun transformImage(frame: ImageFrame, constantData: RotateData): BufferedImage {
        val image = frame.content
        val radians = Math.toRadians(degrees)
        val resultType = constantData.resultImageType
        return image.rotate(radians, resultType, backgroundColor)
    }

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String
    ): RotateData {
        val firstImage = firstFrame.content
        val resultType = firstImage.supportedTransparentImageType(outputFormat)
        return RotateData(resultType)
    }
}

@JvmInline
private value class RotateData(
    val resultImageType: Int,
)
