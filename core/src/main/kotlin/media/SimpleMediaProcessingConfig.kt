package com.shakster.borgar.core.media

import com.shakster.borgar.core.media.reader.ImageReader
import com.shakster.borgar.core.media.reader.transform

open class SimpleMediaProcessingConfig(
    val processor: ImageProcessor<*>,
    override val outputName: String = "",
) : MediaProcessingConfig {

    override suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader {
        return imageReader.transform(processor, outputFormat)
    }

    override fun toString(): String {
        return "SimpleMediaProcessingConfig(processor=$processor, outputName='$outputName')"
    }
}
