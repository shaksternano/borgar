package com.shakster.borgar.core.media

import com.shakster.borgar.core.io.SuspendCloseable
import com.shakster.borgar.core.io.closeAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.awt.image.BufferedImage

interface ImageProcessor<T : Any> : SuspendCloseable {

    suspend fun constantData(firstFrame: ImageFrame, imageSource: Flow<ImageFrame>, outputFormat: String): T

    suspend fun transformImage(frame: ImageFrame, constantData: T): BufferedImage

    override suspend fun close() = Unit
}

infix fun <T : Any, U : Any> ImageProcessor<T>.then(after: ImageProcessor<U>): ImageProcessor<*> {
    return if (this is IdentityImageProcessor) {
        after
    } else if (after is IdentityImageProcessor) {
        this
    } else {
        ChainedImageProcessor(this, after)
    }
}

private class ChainedImageProcessor<T : Any, U : Any>(
    private val first: ImageProcessor<T>,
    private val second: ImageProcessor<U>,
) : ImageProcessor<Pair<T, U>> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String,
    ): Pair<T, U> {
        val firstData = first.constantData(firstFrame, imageSource, outputFormat)
        val firstTransformed = firstFrame.copy(
            content = first.transformImage(firstFrame, firstData)
        )
        val firstTransformedFlow = imageSource.map {
            it.copy(
                content = first.transformImage(it, firstData)
            )
        }
        val secondData = second.constantData(firstTransformed, firstTransformedFlow, outputFormat)
        return firstData to secondData
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: Pair<T, U>): BufferedImage {
        val firstTransformed = frame.copy(
            content = first.transformImage(frame, constantData.first)
        )
        return second.transformImage(firstTransformed, constantData.second)
    }

    override suspend fun close() = closeAll(
        first,
        second,
    )

    override fun toString(): String {
        return "ChainedImageProcessor(first=$first, second=$second)"
    }
}

object IdentityImageProcessor : ImageProcessor<Unit> {

    override suspend fun constantData(firstFrame: ImageFrame, imageSource: Flow<ImageFrame>, outputFormat: String) =
        Unit

    override suspend fun transformImage(frame: ImageFrame, constantData: Unit): BufferedImage = frame.content
}
