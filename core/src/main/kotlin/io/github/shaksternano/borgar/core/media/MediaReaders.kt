package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.collect.putAllKeys
import io.github.shaksternano.borgar.core.exception.UnreadableFileException
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.fileFormat
import io.github.shaksternano.borgar.core.media.reader.*

private val imageReaderFactories: MutableMap<String, ImageReaderFactory> = mutableMapOf()

private val audioReaderFactories: MutableMap<String, AudioReaderFactory> = mutableMapOf()

@Suppress("unused")
private val init: Unit = run {
    registerImageOnlyFactory(JavaxImageReader.Factory)
    registerImageOnlyFactory(GifReader.Factory)
    registerImageOnlyFactory(WebPImageReader.Factory)
    registerImageOnlyFactory(PdfReader.Factory)
}

private fun registerImageFactory(factory: ImageReaderFactory) =
    imageReaderFactories.putAllKeys(
        factory.supportedFormats,
        factory,
    )

private fun registerAudioFactory(factory: AudioReaderFactory) =
    audioReaderFactories.putAllKeys(
        factory.supportedFormats,
        factory,
    )

private fun registerImageOnlyFactory(factory: ImageReaderFactory) {
    registerImageFactory(factory)
    registerAudioFactory(NoAudioReader.Factory(factory.supportedFormats))
}

fun isReaderFormatSupported(format: String): Boolean =
    imageReaderFactories.containsKey(format)

suspend fun createImageReader(input: DataSource): ImageReader =
    createImageReader(input, input.fileFormat())

suspend fun createImageReader(input: DataSource, format: String): ImageReader {
    val factory = imageReaderFactories.getOrDefault(format, FFmpegImageReader.Factory)
    return try {
        factory.create(input)
    } catch (t: Throwable) {
        throw UnreadableFileException(t)
    }
}

suspend fun createAudioReader(input: DataSource): AudioReader =
    createAudioReader(input, input.fileFormat())

suspend fun createAudioReader(input: DataSource, format: String): AudioReader {
    val factory = audioReaderFactories.getOrDefault(format, FFmpegAudioReader.Factory)
    return try {
        factory.create(input)
    } catch (t: Throwable) {
        throw UnreadableFileException(t)
    }
}
