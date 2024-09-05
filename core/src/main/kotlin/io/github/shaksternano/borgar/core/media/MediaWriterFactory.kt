package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.media.writer.MediaWriter
import java.nio.file.Path
import kotlin.time.Duration

interface MediaWriterFactory {

    val supportedFormats: Set<String>
    val maxImageDimension: Int
        get() = 0
    val requiredImageType: Int
        get() = 0

    suspend fun create(
        output: Path,
        outputFormat: String,
        loopCount: Int,
        audioChannels: Int,
        audioSampleRate: Int,
        audioBitrate: Int,
        maxFileSize: Long,
        maxDuration: Duration,
    ): MediaWriter
}
