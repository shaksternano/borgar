package io.github.shaksternano.borgar.core.media.writer

import com.shakster.gifkt.GifEncoder
import com.shakster.gifkt.ParallelGifEncoder
import io.github.shaksternano.borgar.core.AVAILABLE_PROCESSORS
import io.github.shaksternano.borgar.core.io.IO_DISPATCHER
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.MediaWriterFactory
import kotlinx.coroutines.withContext
import java.nio.file.Path
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
            val encoder = withContext(IO_DISPATCHER) {
                GifEncoder.builder(output)
            }.apply {
                transparencyColorTolerance = 0.01
                quantizedTransparencyColorTolerance = 0.02
                this.loopCount = loopCount
                comment = "GIF created with https://github.com/shaksternano/borgar"
                maxConcurrency = AVAILABLE_PROCESSORS
                ioContext = IO_DISPATCHER
            }.buildParallel()
            return GifWriter(encoder)
        }
    }
}
