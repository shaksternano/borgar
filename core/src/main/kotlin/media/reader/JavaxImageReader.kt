package com.shakster.borgar.core.media.reader

import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.io.IO_DISPATCHER
import com.shakster.borgar.core.media.ImageFrame
import com.shakster.borgar.core.media.ImageReaderFactory
import com.shakster.borgar.core.media.addTransparency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class JavaxImageReader(
    image: BufferedImage,
) : BaseImageReader() {

    private val frame: ImageFrame = run {
        // For some reason some images have a greyscale type, even though they have color
        val converted = image.addTransparency()
        ImageFrame(converted, 1.milliseconds, Duration.ZERO)
    }

    override val frameCount: Int = 1
    override val frameRate: Double = 1.0
    override val duration: Duration = 1.milliseconds
    override val frameDuration: Duration = 1.milliseconds
    override val width: Int = frame.content.width
    override val height: Int = frame.content.height
    override val loopCount: Int = 0

    override suspend fun readFrame(timestamp: Duration): ImageFrame = frame

    override fun asFlow(): Flow<ImageFrame> = flowOf(frame)

    override suspend fun reversed(): MediaReader<ImageFrame> = this

    override suspend fun createChangedSpeed(speedMultiplier: Double): MediaReader<ImageFrame> = this

    override suspend fun close() = Unit

    object Factory : ImageReaderFactory {
        override val supportedFormats: Set<String> = setOf(
            "bmp",
            "jpeg",
            "jpg",
            "wbmp",
            "png",
            "gif",
            "tif",
            "tiff",
        )

        override suspend fun create(input: DataSource): ImageReader = input.newStream().use {
            val image = withContext(IO_DISPATCHER) {
                ImageIO.read(it)
            } ?: throw IllegalArgumentException("Failed to read image")
            JavaxImageReader(image)
        }
    }
}
