package io.github.shaksternano.borgar.core.media.writer

import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.MediaWriterFactory
import io.github.shaksternano.borgar.core.media.bound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.ifok.image.image4j.codec.ico.ICOEncoder
import java.nio.file.Path
import kotlin.time.Duration

private const val MAX_DIMENSION = 256

class Image4jIcoWriter(
    private val output: Path,
) : NoAudioWriter() {

    override val isStatic: Boolean = true
    private var written: Boolean = false

    override suspend fun writeImageFrame(frame: ImageFrame) {
        if (written) return
        written = true
        val image = frame.content.bound(MAX_DIMENSION)
        withContext(Dispatchers.IO) {
            ICOEncoder.write(image, output.toFile())
        }
    }

    override suspend fun close() = Unit

    object Factory : MediaWriterFactory {
        override val supportedFormats: Set<String> = setOf(
            "ico",
        )

        override suspend fun create(
            output: Path,
            outputFormat: String,
            loopCount: Int,
            audioChannels: Int,
            audioSampleRate: Int,
            audioBitrate: Int,
            maxFileSize: Long,
            maxDuration: Duration
        ): MediaWriter = Image4jIcoWriter(output)
    }
}
