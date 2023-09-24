package io.github.shaksternano.borgar.core.media.writer

import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.MediaUtil
import io.github.shaksternano.borgar.core.media.MediaWriterFactory
import io.github.shaksternano.borgar.core.media.convertType
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO

class JavaxImageWriter(
    private val output: Path,
    private val outputFormat: String,
) : NoAudioWriter() {

    override val isStatic: Boolean = true
    private var written = false

    override fun writeImageFrame(frame: ImageFrame) {
        if (written) return
        written = true
        val imageType = if (MediaUtil.supportsTransparency(outputFormat)) {
            BufferedImage.TYPE_INT_ARGB
        } else {
            BufferedImage.TYPE_3BYTE_BGR
        }
        val image = frame.content.convertType(imageType)
        val supportedFormat = ImageIO.write(image, outputFormat, output.toFile())
        if (!supportedFormat) throw IllegalArgumentException("Unsupported image format: $outputFormat")
    }

    override fun close() = Unit

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

        override fun create(
            output: Path,
            outputFormat: String,
            loopCount: Int,
            audioChannels: Int,
            audioSampleRate: Int,
            audioBitrate: Int,
            maxFileSize: Long,
            maxDuration: Double
        ): MediaWriter = JavaxImageWriter(output, outputFormat)
    }
}
