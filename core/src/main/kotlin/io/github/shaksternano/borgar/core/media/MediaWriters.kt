package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.media.writer.MediaWriter
import java.nio.file.Path

fun createWriter(
    output: Path,
    outputFormat: String,
    loopCount: Int,
    audioChannels: Int,
    audioSampleRate: Int,
    audioBitrate: Int,
    maxFileSize: Long,
    maxDuration: Double
): MediaWriter {
    TODO()
}
