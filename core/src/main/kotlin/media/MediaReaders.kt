package com.shakster.borgar.core.media

import com.shakster.borgar.core.collect.putAllKeys
import com.shakster.borgar.core.exception.UnreadableFileException
import com.shakster.borgar.core.ffmpegAvailable
import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.io.fileFormat
import com.shakster.borgar.core.media.reader.*

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
    val factory = imageReaderFactories[format] ?: if (ffmpegAvailable) {
        FFmpegImageReader.Factory
    } else {
        throw UnreadableFileException(type = format)
    }
    return try {
        factory.create(input)
    } catch (t: Throwable) {
        throw UnreadableFileException(t, format)
    }
}

suspend fun createAudioReader(input: DataSource): AudioReader =
    createAudioReader(input, input.fileFormat())

suspend fun createAudioReader(input: DataSource, format: String): AudioReader {
    val factory = audioReaderFactories[format] ?: if (ffmpegAvailable) {
        FFmpegAudioReader.Factory
    } else {
        throw UnreadableFileException(type = format)
    }
    return try {
        factory.create(input)
    } catch (t: Throwable) {
        throw UnreadableFileException(t, format)
    }
}
