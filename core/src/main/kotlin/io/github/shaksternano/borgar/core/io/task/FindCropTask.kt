package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.exception.FailedOperationException
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageProcessor
import io.github.shaksternano.borgar.core.media.MediaProcessConfig
import io.github.shaksternano.borgar.core.media.SimpleMediaProcessConfig
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

    override val config: MediaProcessConfig = SimpleMediaProcessConfig(
        processor = FindCropProcessor(
            onlyCheckFirst,
            failureMessage,
            ::findCropArea,
        ),
        outputName = outputName
    )

    protected abstract fun findCropArea(image: BufferedImage): Rectangle
}

private class FindCropProcessor(
    private val onlyCheckFirst: Boolean,
    private val failureMessage: String,
    private val findCropArea: (BufferedImage) -> Rectangle,
) : ImageProcessor<FindCropData> {

    override suspend fun transformImage(frame: ImageFrame, constantData: FindCropData): BufferedImage {
        val toKeep = constantData.toKeep
        val image = frame.content
        return image.getSubimage(toKeep.x, toKeep.y, toKeep.width, toKeep.height)
    }

    override suspend fun constantData(
        firstImage: BufferedImage,
        imageSource: Flow<ImageFrame>,
        outputFormat: String
    ): FindCropData {
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
            throw FailedOperationException(failureMessage)
        } else {
            FindCropData(toKeep)
        }
    }
}

@JvmInline
private value class FindCropData(
    val toKeep: Rectangle,
)
