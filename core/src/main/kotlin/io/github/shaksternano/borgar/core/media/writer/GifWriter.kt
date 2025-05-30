package io.github.shaksternano.borgar.core.media.writer

import com.shakster.gifkt.ParallelGifEncoder
import com.shakster.gifkt.rgb
import io.github.shaksternano.borgar.core.AVAILABLE_PROCESSORS
import io.github.shaksternano.borgar.core.io.IO_DISPATCHER
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.MediaWriterFactory
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import java.nio.file.Path
import kotlin.time.Duration

class GifWriter(
    private val encoder: ParallelGifEncoder,
) : NoAudioWriter() {

    override val isStatic: Boolean = false

    override suspend fun writeImageFrame(frame: ImageFrame) {
        val image = frame.content
        encoder.writeFrame(
            image.rgb,
            image.width,
            image.height,
            frame.duration,
        )
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
            val pathString = output.toAbsolutePath().toString()
            val kotlinPath = kotlinx.io.files.Path(pathString)
            val sink = withContext(IO_DISPATCHER) {
                SystemFileSystem.sink(kotlinPath)
            }.buffered()
            val encoder = ParallelGifEncoder(
                sink = sink,
                loopCount = loopCount,
                transparencyColorTolerance = 0.01,
                quantizedTransparencyColorTolerance = 0.02,
                comment = "GIF created with https://github.com/shaksternano/borgar",
                maxConcurrency = AVAILABLE_PROCESSORS,
                ioContext = IO_DISPATCHER,
            )
            return GifWriter(encoder)
        }
    }
}
