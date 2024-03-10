package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageProcessor
import io.github.shaksternano.borgar.core.media.MediaProcessingConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessingConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.take
import java.awt.Rectangle
import java.awt.image.BufferedImage

abstract class FindCropTask(
    onlyCheckFirst: Boolean,
    outputName: String,
    maxFileSize: Long,
    failureMessage: String,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SimpleMediaProcessingConfig(
        processor = FindCropProcessor(
            onlyCheckFirst,
            failureMessage,
            ::findCropArea,
        ),
        outputName = outputName
    )

    protected abstract suspend fun findCropArea(image: BufferedImage): Rectangle
}

private class FindCropProcessor(
    private val onlyCheckFirst: Boolean,
    private val failureMessage: String,
    private val findCropArea: suspend (BufferedImage) -> Rectangle,
) : ImageProcessor<FindCropData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String
    ): FindCropData {
        val firstImage = firstFrame.content
        val width = firstImage.width
        val height = firstImage.height
        val newImageSource = if (onlyCheckFirst) {
            imageSource.take(1)
        } else {
            imageSource
        }
        val toKeep = newImageSource.fold(Rectangle()) { keepArea, frame ->
            val image = frame.content
            val mayKeepArea = findCropArea(image)
            if ((mayKeepArea.x != 0
                    || mayKeepArea.y != 0
                    || mayKeepArea.width != width
                    || mayKeepArea.height != height)
                && (mayKeepArea.width > 0)
                && (mayKeepArea.height > 0)
                && keepArea.width != 0
                && keepArea.height != 0
            ) {
                keepArea.union(mayKeepArea)
            } else {
                mayKeepArea
            }
        }
        return if (toKeep.x == 0
            && toKeep.y == 0
            && toKeep.width == width
            && toKeep.height == height
        ) {
            throw ErrorResponseException(failureMessage)
        } else {
            FindCropData(toKeep)
        }
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: FindCropData): BufferedImage {
        val toKeep = constantData.toKeep
        val image = frame.content
        val imageArea = Rectangle(
            0,
            0,
            image.width,
            image.height,
        )
        val cropArea = imageArea.intersection(toKeep)
        return image.getSubimage(
            cropArea.x,
            cropArea.y,
            cropArea.width,
            cropArea.height,
        )
    }
}

@JvmInline
private value class FindCropData(
    val toKeep: Rectangle,
)
