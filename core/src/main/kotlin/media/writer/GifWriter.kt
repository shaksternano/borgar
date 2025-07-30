package com.shakster.borgar.core.media.writer

import com.shakster.borgar.core.AVAILABLE_PROCESSORS
import com.shakster.borgar.core.io.IO_DISPATCHER
import com.shakster.borgar.core.media.ImageFrame
import com.shakster.borgar.core.media.MediaWriterFactory
import com.shakster.gifkt.ParallelGifEncoder
import kotlinx.coroutines.withContext
import kotlinx.io.asSink
import kotlinx.io.buffered
import java.nio.file.Path
import kotlin.io.path.outputStream
import kotlin.time.Duration

class GifWriter(
    private val encoder: ParallelGifEncoder,
) : NoAudioWriter() {

    override val isStatic: Boolean = false

    override suspend fun writeImageFrame(frame: ImageFrame) {
        encoder.writeFrame(frame.content, frame.duration)
    }

    override suspend fun close() {
        encoder.close()
    }

    object Factory : MediaWriterFactory {

        override val supportedFormats: Set<String> = setOf("gif")

        /**
         * 480p
         */
        override val maxImageDimension: Int = 854

        override suspend fun create(
            output: Path,
            outputFormat: String,
            loopCount: Int,
            audioChannels: Int,
            audioSampleRate: Int,
            audioBitrate: Int,
            maxFileSize: Long,
            maxDuration: Duration,
        ): MediaWriter {
            val sink = withContext(IO_DISPATCHER) {
                output.outputStream().asSink().buffered()
            }
            val encoder = ParallelGifEncoder(
                sink,
                colorDifferenceTolerance = 0.01,
                quantizedColorDifferenceTolerance = 0.02,
                loopCount = loopCount,
                comment = "GIF created with https://github.com/shaksternano/borgar",
                maxConcurrency = AVAILABLE_PROCESSORS,
                ioContext = IO_DISPATCHER,
            )
            return GifWriter(encoder)
        }
    }
}
