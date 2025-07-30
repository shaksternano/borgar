package com.shakster.borgar.core.media.reader

import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.io.IO_DISPATCHER
import com.shakster.borgar.core.media.ImageFrame
import com.shakster.borgar.core.media.ImageReaderFactory
import com.shakster.borgar.core.media.getCircularTimestamp
import com.shakster.gifkt.GifDecoder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import kotlin.time.Duration

class GifReader(
    private val decoder: GifDecoder,
) : BaseImageReader() {

    override val frameCount: Int = decoder.frameCount
    override val frameDuration: Duration
        get() = decoder.frameInfos.minOf { it.duration }
    override val duration: Duration = decoder.duration
    override val frameRate: Double = 1000.0 / frameDuration.inWholeMilliseconds
    override val width: Int = decoder.width
    override val height: Int = decoder.height
    override val loopCount: Int = decoder.loopCount

    override suspend fun readFrame(timestamp: Duration): ImageFrame {
        val circularTimestamp = timestamp.getCircularTimestamp(duration)
        val frame = withContext(IO_DISPATCHER) {
            decoder[circularTimestamp]
        }
        return ImageFrame(
            frame.toBufferedImage(),
            frame.duration,
            frame.timestamp,
        )
    }

    override fun asFlow(): Flow<ImageFrame> = channelFlow {
        withContext(IO_DISPATCHER) {
            decoder.asSequence().forEach { frame ->
                send(
                    ImageFrame(
                        frame.toBufferedImage(),
                        frame.duration,
                        frame.timestamp,
                    )
                )
            }
        }
    }

    override suspend fun close() {
        withContext(IO_DISPATCHER) {
            decoder.close()
        }
    }

    object Factory : ImageReaderFactory {

        override val supportedFormats: Set<String> = setOf("gif")

        override suspend fun create(input: DataSource): ImageReader {
            val fileInput = input.getOrWriteFile()
            val decoder = withContext(IO_DISPATCHER) {
                GifDecoder(
                    fileInput.path,
                    cacheFrameInterval = 20,
                )
            }
            return GifReader(decoder)
        }
    }
}
