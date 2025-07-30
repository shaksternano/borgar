package com.shakster.borgar.core.task

import com.shakster.borgar.core.media.MediaProcessingConfig
import com.shakster.borgar.core.media.reader.ConstantFrameDurationMediaReader
import com.shakster.borgar.core.media.reader.ImageReader

class ReduceFpsTask(
    fpsReductionRatio: Double,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = ReduceFpsConfig(fpsReductionRatio)
}

private class ReduceFpsConfig(
    private val fpsReductionRatio: Double,
) : MediaProcessingConfig {

    override suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader {
        val frameDuration = imageReader.frameDuration * fpsReductionRatio
        return ConstantFrameDurationMediaReader(imageReader, frameDuration)
    }
}
