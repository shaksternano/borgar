package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.media.MediaProcessConfig
import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader

class SpeedTask(
    speed: Double,
    maxFileSize: Long,
    outputName: String = "speed",
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessConfig = SpeedConfig(speed, outputName)
}

private class SpeedConfig(
    private val speed: Double,
    override val outputName: String
) : MediaProcessConfig {

    override suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader =
        imageReader.changeSpeed(speed)

    override suspend fun transformAudioReader(audioReader: AudioReader): AudioReader =
        audioReader.changeSpeed(speed)
}
