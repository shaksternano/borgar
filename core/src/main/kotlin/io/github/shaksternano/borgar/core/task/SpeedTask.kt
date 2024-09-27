package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.media.MediaProcessingConfig
import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader

class SpeedTask(
    speed: Double,
    maxFileSize: Long,
    outputName: String = "",
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SpeedConfig(speed, outputName)
}

private class SpeedConfig(
    private val speed: Double,
    override val outputName: String
) : MediaProcessingConfig {

    override suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader =
        imageReader.changeSpeed(speed)

    override suspend fun transformAudioReader(audioReader: AudioReader, outputFormat: String): AudioReader =
        audioReader.changeSpeed(speed)
}
