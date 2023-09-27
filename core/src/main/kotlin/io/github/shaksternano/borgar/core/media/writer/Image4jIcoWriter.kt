package io.github.shaksternano.borgar.core.media.writer

import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.MediaWriterFactory
import io.github.shaksternano.borgar.core.media.bound
import net.ifok.image.image4j.codec.ico.ICOEncoder
import java.io.File
import java.nio.file.Path
import kotlin.time.Duration

private const val MAX_DIMENSION = 256

class Image4jIcoWriter(
    output: Path,
) : NoAudioWriter() {

    override val isStatic: Boolean = true
    private val output: File = output.toFile()
    private var written: Boolean = false

    override fun writeImageFrame(frame: ImageFrame) {
        if (!written) {
            written = true
            val image = frame.content.bound(MAX_DIMENSION)
            ICOEncoder.write(image, output)
        }
    }

    override fun close() = Unit

    object Factory : MediaWriterFactory {
        override val supportedFormats: Set<String> = setOf(
            "ico",
        )

        override fun create(
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
