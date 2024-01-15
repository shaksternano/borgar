package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.awt.image.BufferedImage
import kotlin.math.abs

interface ImageProcessor<T> : SuspendCloseable {

    val speed: Float
        get() = 1.0F

    suspend fun transformImage(frame: ImageFrame, constantData: T): BufferedImage

    suspend fun constantData(firstImage: BufferedImage, imageSource: Flow<ImageFrame>): T

    infix fun <V> then(after: ImageProcessor<V>): ImageProcessor<Pair<T, V>> {
        return object : ImageProcessor<Pair<T, V>> {
            override val speed: Float = this@ImageProcessor.speed * after.speed

            override suspend fun transformImage(frame: ImageFrame, constantData: Pair<T, V>): BufferedImage {
                val firstTransformed = this@ImageProcessor.transformImage(frame, constantData.first)
                return after.transformImage(frame.copy(content = firstTransformed), constantData.second)
            }

            override suspend fun constantData(firstImage: BufferedImage, imageSource: Flow<ImageFrame>): Pair<T, V> {
                val firstConstantData = this@ImageProcessor.constantData(firstImage, imageSource)
                val newImageSource = imageSource.map {
                    val newImage = this@ImageProcessor.transformImage(it, firstConstantData)
                    it.copy(content = newImage)
                }
                return firstConstantData to after.constantData(firstImage, newImageSource)
            }

            override suspend fun close() = closeAll(
                this@ImageProcessor,
                after,
            )
        }
    }

    override suspend fun close() = Unit
}

val ImageProcessor<*>.absoluteSpeed: Float
    get() = abs(speed)

abstract class DualImageProcessor<T> : ImageProcessor<T> {
    lateinit var frame2: ImageFrame
}
