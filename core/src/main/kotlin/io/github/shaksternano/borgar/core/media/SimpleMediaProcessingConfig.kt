package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.reader.transform
import java.awt.image.BufferedImage

open class SimpleMediaProcessingConfig(
    val processor: ImageProcessor<*>,
    override val outputName: String = "",
) : MediaProcessingConfig {

    constructor(
        outputName: String = "",
        transform: (ImageFrame) -> BufferedImage,
    ) : this(
        SimpleImageProcessor(transform),
        outputName,
    )

    override suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader {
        return imageReader.transform(processor, outputFormat)
    }

    override fun toString(): String {
        return "SimpleMediaProcessingConfig(processor=$processor, outputName='$outputName')"
    }
}