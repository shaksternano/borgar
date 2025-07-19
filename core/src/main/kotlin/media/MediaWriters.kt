package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.AVAILABLE_PROCESSORS
import io.github.shaksternano.borgar.core.collect.putAllKeys
import io.github.shaksternano.borgar.core.media.writer.*
import io.github.shaksternano.borgar.core.util.then
import java.awt.image.BufferedImage
import java.nio.file.Path
import kotlin.time.Duration

private val writerFactories: Map<String, MediaWriterFactory> = buildMap {
    registerFactory(JavaxImageWriter.Factory)
    registerFactory(GifWriter.Factory)
    registerFactory(Image4jIcoWriter.Factory)
}

private fun MutableMap<String, MediaWriterFactory>.registerFactory(
    factory: MediaWriterFactory,
) = putAllKeys(
    factory.supportedFormats,
    factory,
)

fun isWriterFormatSupported(format: String): Boolean =
    writerFactories.containsKey(format)

suspend fun createWriter(
    output: Path,
    outputFormat: String,
    loopCount: Int,
    audioChannels: Int,
    audioSampleRate: Int,
    audioBitrate: Int,
    maxFileSize: Long,
    maxDuration: Duration,
): MediaWriter {
    val factory = writerFactories.getOrDefault(outputFormat, FFmpegVideoWriter.Factory)
    val writer = factory.create(
        output,
        outputFormat,
        loopCount,
        audioChannels,
        audioSampleRate,
        audioBitrate,
        maxFileSize,
        maxDuration,
    )
    var preProcessing: ((BufferedImage) -> BufferedImage)? = null
    if (factory.maxImageDimension > 0) {
        preProcessing = { it.bound(factory.maxImageDimension) }
    }
    if (factory.requiredImageType > 0) {
        preProcessing = preProcessing.then { it.convertType(factory.requiredImageType) }
    }
    return if (preProcessing == null) {
        writer
    } else {
        PreProcessingWriter(
            writer = writer,
            maxConcurrency = AVAILABLE_PROCESSORS,
            preProcessImage = preProcessing,
        )
    }
}
