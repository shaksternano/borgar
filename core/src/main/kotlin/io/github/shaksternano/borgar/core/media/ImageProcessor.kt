package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.awt.image.BufferedImage

interface ImageProcessor<T> : SuspendCloseable {

    val speed: Double
        get() = 1.0

    suspend fun transformImage(frame: ImageFrame, constantData: T): BufferedImage

    suspend fun constantData(firstImage: BufferedImage, imageSource: Flow<ImageFrame>, outputFormat: String): T

    infix fun <V> then(after: ImageProcessor<V>): ImageProcessor<Pair<T, V>> = object : ImageProcessor<Pair<T, V>> {

        override val speed: Double = this@ImageProcessor.speed * after.speed

        override suspend fun transformImage(frame: ImageFrame, constantData: Pair<T, V>): BufferedImage {
            val firstTransformed = this@ImageProcessor.transformImage(frame, constantData.first)
            return after.transformImage(frame.copy(content = firstTransformed), constantData.second)
        }

        override suspend fun constantData(
            firstImage: BufferedImage,
            imageSource: Flow<ImageFrame>,
            outputFormat: String
        ): Pair<T, V> {
            val firstConstantData = this@ImageProcessor.constantData(firstImage, imageSource, outputFormat)
            val newImageSource = imageSource.map {
                val newImage = this@ImageProcessor.transformImage(it, firstConstantData)
                it.copy(content = newImage)
            }
            return firstConstantData to after.constantData(firstImage, newImageSource, outputFormat)
        }

        override suspend fun close() = closeAll(
            this@ImageProcessor,
            after,
        )
    }

    override suspend fun close() = Unit
}

abstract class DualImageProcessor<T> : ImageProcessor<T> {
    lateinit var frame2: ImageFrame
}

class SimpleImageProcessor(
    private val transform: (ImageFrame) -> BufferedImage,
) : ImageProcessor<Unit> {

    override suspend fun transformImage(frame: ImageFrame, constantData: Unit): BufferedImage =
        transform(frame)

    override suspend fun constantData(firstImage: BufferedImage, imageSource: Flow<ImageFrame>, outputFormat: String) =
        Unit
}

object IdentityImageProcessor : ImageProcessor<Unit> {

    override suspend fun transformImage(frame: ImageFrame, constantData: Unit): BufferedImage = frame.content

    override suspend fun constantData(firstImage: BufferedImage, imageSource: Flow<ImageFrame>, outputFormat: String) =
        Unit
}
