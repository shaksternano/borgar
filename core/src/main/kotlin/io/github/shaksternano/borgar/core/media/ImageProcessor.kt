package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.io.SuspendCloseable
import kotlinx.coroutines.flow.Flow
import java.awt.image.BufferedImage

interface ImageProcessor<T : Any> : SuspendCloseable {

    suspend fun constantData(firstFrame: ImageFrame, imageSource: Flow<ImageFrame>, outputFormat: String): T

    suspend fun transformImage(frame: ImageFrame, constantData: T): BufferedImage

    override suspend fun close() = Unit
}

class SimpleImageProcessor(
    private val transform: (ImageFrame) -> BufferedImage,
) : ImageProcessor<Unit> {

    override suspend fun constantData(firstFrame: ImageFrame, imageSource: Flow<ImageFrame>, outputFormat: String) =
        Unit

    override suspend fun transformImage(frame: ImageFrame, constantData: Unit): BufferedImage = transform(frame)
}
