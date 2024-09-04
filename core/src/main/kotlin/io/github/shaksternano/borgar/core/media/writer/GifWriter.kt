package io.github.shaksternano.borgar.core.media.writer

import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.MediaWriterFactory
import io.github.shaksternano.borgar.core.media.bound
import io.github.shaksternano.borgar.core.media.rgb
import io.github.shaksternano.gifcodec.ParallelGifEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import java.nio.file.Path
import kotlin.time.Duration

// 480p
private const val MAX_DIMENSION: Int = 854
private val MAX_CONCURRENCY: Int = Runtime.getRuntime().availableProcessors() * 2

class GifWriter(
    private val encoder: ParallelGifEncoder,
) : NoAudioWriter() {

    override val isStatic: Boolean = false

    override suspend fun writeImageFrame(frame: ImageFrame) {
        val image = frame.content.bound(MAX_DIMENSION)
        val rgb = image.rgb
        encoder.writeFrame(
            rgb,
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
            val sink = withContext(Dispatchers.IO) {
                SystemFileSystem.sink(kotlinPath)
            }.buffered()
            val encoder = ParallelGifEncoder(
                sink = sink,
                loopCount = loopCount,
                transparencyColorTolerance = 0.01,
                quantizedTransparencyColorTolerance = 0.02,
                comment = "GIF created with https://github.com/shaksternano/borgar",
                maxConcurrency = MAX_CONCURRENCY,
            )
            return GifWriter(encoder)
        }
    }
}
