package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageProcessor
import io.github.shaksternano.borgar.core.media.MediaProcessConfig
import kotlinx.coroutines.flow.Flow
import java.awt.image.BufferedImage

class SpeedTask(
    speed: Double,
    maxFileSize: Long,
    outputName: String = "speed",
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessConfig = SpeedConfig(speed, outputName)
}

private class SpeedConfig(
    speed: Double,
    override val outputName: String
) : MediaProcessConfig {

    override val processor: ImageProcessor<out Any> = SpeedProcessor(speed)
}

private class SpeedProcessor(
    override val speed: Double
) : ImageProcessor<Unit> {

    override suspend fun transformImage(frame: ImageFrame, constantData: Unit): BufferedImage =
        frame.content

    override suspend fun constantData(firstFrame: ImageFrame, imageSource: Flow<ImageFrame>, outputFormat: String) =
        Unit
}
