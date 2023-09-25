package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.collect.putAllKeys
import io.github.shaksternano.borgar.core.media.writer.JavaxImageWriter
import io.github.shaksternano.borgar.core.media.writer.MediaWriter
import io.github.shaksternano.borgar.core.media.writer.ScrimageGifWriter
import java.nio.file.Path
import kotlin.time.Duration

private val writerFactories: Map<String, MediaWriterFactory> = buildMap {
    registerFactory(JavaxImageWriter.Factory)
    registerFactory(ScrimageGifWriter.Factory)
}

private fun MutableMap<String, MediaWriterFactory>.registerFactory(
    factory: MediaWriterFactory,
) = putAllKeys(
    factory.supportedFormats,
    factory,
)

fun createWriter(
    output: Path,
    outputFormat: String,
    loopCount: Int,
    audioChannels: Int,
    audioSampleRate: Int,
    audioBitrate: Int,
    maxFileSize: Long,
    maxDuration: Duration,
): MediaWriter {
    val factory = writerFactories.getOrDefault(outputFormat, JavaxImageWriter.Factory)
    return factory.create(
        output,
        outputFormat,
        loopCount,
        audioChannels,
        audioSampleRate,
        audioBitrate,
        maxFileSize,
        maxDuration,
    )
}
