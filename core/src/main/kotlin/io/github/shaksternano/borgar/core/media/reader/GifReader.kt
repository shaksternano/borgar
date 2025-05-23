package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import io.github.shaksternano.borgar.core.media.getCircularTimestamp
import io.github.shaksternano.borgar.core.media.rgb
import io.github.shaksternano.gifcodec.GifDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import java.awt.image.BufferedImage
import kotlin.time.Duration

class GifReader(
    private val decoder: GifDecoder,
) : BaseImageReader() {

    override val frameCount: Int = decoder.frameCount
    override val frameDuration: Duration = decoder.frameInfos.minOf { it.duration }
    override val duration: Duration = decoder.duration
    override val frameRate: Double = 1000.0 / frameDuration.inWholeMilliseconds
    override val width: Int = decoder.width
    override val height: Int = decoder.height
    override val loopCount: Int = decoder.loopCount

    override suspend fun readFrame(timestamp: Duration): ImageFrame {
        val circularTimestamp = timestamp.getCircularTimestamp(duration)
        val frame = withContext(Dispatchers.IO) {
            decoder[circularTimestamp]
        }
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        image.rgb = frame.argb
        return ImageFrame(
            image,
            frame.duration,
            frame.timestamp,
        )
    }

    override fun asFlow(): Flow<ImageFrame> = channelFlow {
        withContext(Dispatchers.IO) {
            decoder.asSequence().forEach { frame ->
                val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                image.rgb = frame.argb
                send(
                    ImageFrame(
                        image,
                        frame.duration,
                        frame.timestamp,
                    )
                )
            }
        }
    }

    override suspend fun close() {
        withContext(Dispatchers.IO) {
            decoder.close()
        }
    }

    object Factory : ImageReaderFactory {

        override val supportedFormats: Set<String> = setOf("gif")

        override suspend fun create(input: DataSource): ImageReader {
            val fileInput = input.getOrWriteFile()
            val path = Path(fileInput.path.toString())
            val decoder = withContext(Dispatchers.IO) {
                GifDecoder(
                    path,
                    cacheFrameInterval = 20,
                )
            }
            return GifReader(decoder)
        }
    }
}
