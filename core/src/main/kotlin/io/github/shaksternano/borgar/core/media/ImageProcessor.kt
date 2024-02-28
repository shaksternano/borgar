package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.awt.image.BufferedImage

interface ImageProcessor<T> : SuspendCloseable {

    suspend fun constantData(firstFrame: ImageFrame, imageSource: Flow<ImageFrame>, outputFormat: String): T

    suspend fun transformImage(frame: ImageFrame, constantData: T): BufferedImage

    infix fun <V> then(after: ImageProcessor<V>): ImageProcessor<Pair<T, V>> = object : ImageProcessor<Pair<T, V>> {

        override suspend fun constantData(
            firstFrame: ImageFrame,
            imageSource: Flow<ImageFrame>,
            outputFormat: String
        ): Pair<T, V> {
            val firstConstantData = this@ImageProcessor.constantData(firstFrame, imageSource, outputFormat)
            val newFirstFrame = firstFrame.copy(
                content = this@ImageProcessor.transformImage(firstFrame, firstConstantData)
            )
            val newImageSource = imageSource.map {
                val newImage = this@ImageProcessor.transformImage(it, firstConstantData)
                it.copy(content = newImage)
            }
            return firstConstantData to after.constantData(newFirstFrame, newImageSource, outputFormat)
        }

        override suspend fun transformImage(frame: ImageFrame, constantData: Pair<T, V>): BufferedImage {
            val firstTransformed = this@ImageProcessor.transformImage(frame, constantData.first)
            return after.transformImage(frame.copy(content = firstTransformed), constantData.second)
        }

        override suspend fun close() = closeAll(
            this@ImageProcessor,
            after,
        )
    }

    override suspend fun close() = Unit
}

class SimpleImageProcessor(
    private val transform: (ImageFrame) -> BufferedImage,
) : ImageProcessor<Unit> {

    override suspend fun constantData(firstFrame: ImageFrame, imageSource: Flow<ImageFrame>, outputFormat: String) =
        Unit

    override suspend fun transformImage(frame: ImageFrame, constantData: Unit): BufferedImage = transform(frame)
}
