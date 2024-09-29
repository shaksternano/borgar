package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.media.*
import io.github.shaksternano.borgar.core.media.reader.ConstantFrameDurationMediaReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.reader.transform
import kotlinx.coroutines.flow.Flow
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SpinTask(
    spinSpeed: Double,
    backgroundColor: Color?,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SpinConfig(spinSpeed, backgroundColor)
}

private val SPIN_FRAME_DURATION = 20.milliseconds
private const val BASE_FRAMES_PER_ROTATION = 150.0

private data class SpinConfig(
    private val spinSpeed: Double,
    private val backgroundColor: Color?,
) : MediaProcessingConfig {

    private val rotationDuration: Duration = run {
        val absoluteSpeed = abs(spinSpeed)
        val framesPerRotation = if (absoluteSpeed > 1) {
            max(BASE_FRAMES_PER_ROTATION / absoluteSpeed, 1.0)
        } else {
            BASE_FRAMES_PER_ROTATION
        }
        SPIN_FRAME_DURATION * framesPerRotation
    }

    override suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader {
        val mediaDuration = imageReader.duration
        val rotations = ceil(mediaDuration / rotationDuration)
        val totalDuration = rotationDuration * rotations
        val processor = SpinProcessor(
            spinSpeed,
            rotationDuration,
            backgroundColor,
        )
        return ConstantFrameDurationMediaReader(imageReader, SPIN_FRAME_DURATION, totalDuration).transform(
            processor,
            outputFormat,
        )
    }

    override fun transformOutputFormat(inputFormat: String): String {
        return if (isStaticOnly(inputFormat)) "gif"
        else inputFormat
    }
}

private class SpinProcessor(
    private val spinSpeed: Double,
    private val rotationDuration: Duration,
    private val backgroundColor: Color?,
) : ImageProcessor<SpinData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String
    ): SpinData {
        val firstImage = firstFrame.content
        return SpinData(
            firstImage.supportedTransparentImageType(outputFormat),
            max(firstImage.width, firstImage.height),
        )
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: SpinData): BufferedImage {
        val image = frame.content
        var angle = 2 * Math.PI * (frame.timestamp / rotationDuration)
        if (spinSpeed < 0) {
            angle = -angle
        }
        return image.rotate(
            radians = angle,
            resultType = constantData.imageType,
            backgroundColor = backgroundColor,
            newWidth = constantData.maxDimension,
            newHeight = constantData.maxDimension,
        )
    }
}

private class SpinData(
    val imageType: Int,
    val maxDimension: Int,
)
