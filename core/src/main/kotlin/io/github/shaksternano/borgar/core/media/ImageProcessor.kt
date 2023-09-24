package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.io.closeAll
import java.awt.image.BufferedImage
import java.io.Closeable
import kotlin.math.abs

interface ImageProcessor<T> : Closeable {

    val speed: Float
        get() = 1.0F

    fun transformImage(frame: ImageFrame, constantData: T): BufferedImage

    suspend fun constantData(image: BufferedImage): T

    infix fun <V> then(after: ImageProcessor<V>): ImageProcessor<Pair<T, V>> {
        return object : ImageProcessor<Pair<T, V>> {
            override val speed: Float = this@ImageProcessor.speed * after.speed

            override fun transformImage(frame: ImageFrame, constantData: Pair<T, V>): BufferedImage {
                val firstTransformed = this@ImageProcessor.transformImage(frame, constantData.first)
                return after.transformImage(frame.copy(content = firstTransformed), constantData.second)
            }

            override suspend fun constantData(image: BufferedImage): Pair<T, V> {
                return this@ImageProcessor.constantData(image) to after.constantData(image)
            }

            override fun close() {
                closeAll(this@ImageProcessor, after)
            }
        }
    }

    override fun close() {
    }
}

val ImageProcessor<*>.absoluteSpeed: Float
    get() = abs(speed)

abstract class DualImageProcessor<T> : ImageProcessor<T> {
    lateinit var frame2: ImageFrame
}
