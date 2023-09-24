package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.media.writer.MediaWriter
import java.nio.file.Path

interface MediaWriterFactory {

    val supportedFormats: Set<String>

    fun create(
        output: Path,
        outputFormat: String,
        loopCount: Int,
        audioChannels: Int,
        audioSampleRate: Int,
        audioBitrate: Int,
        maxFileSize: Long,
        maxDuration: Double,
    ): MediaWriter
}
