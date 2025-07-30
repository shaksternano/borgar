package com.shakster.borgar.core.media.writer

import com.shakster.borgar.core.io.IO_DISPATCHER
import com.shakster.borgar.core.media.ImageFrame
import com.shakster.borgar.core.media.MediaWriterFactory
import com.shakster.borgar.core.media.convertType
import com.shakster.borgar.core.media.supportsTransparency
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.time.Duration

class JavaxImageWriter(
    private val output: Path,
    private val outputFormat: String,
) : NoAudioWriter() {

    override val isStatic: Boolean = true
    private var written = false

    override suspend fun writeImageFrame(frame: ImageFrame) {
        if (written) return
        written = true
        val imageType = if (supportsTransparency(outputFormat)) {
            BufferedImage.TYPE_INT_ARGB
        } else {
            BufferedImage.TYPE_3BYTE_BGR
        }
        val image = frame.content.convertType(imageType)
        val supportedFormat = withContext(IO_DISPATCHER) {
            ImageIO.write(image, outputFormat, output.toFile())
        }
        require(supportedFormat) { "Unsupported image format: $outputFormat" }
    }

    override suspend fun close() = Unit

    object Factory : MediaWriterFactory {
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

        override suspend fun create(
            output: Path,
            outputFormat: String,
            loopCount: Int,
            audioChannels: Int,
            audioSampleRate: Int,
            audioBitrate: Int,
            maxFileSize: Long,
            maxDuration: Duration
        ): MediaWriter = JavaxImageWriter(output, outputFormat)
    }
}
