package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.collect.putAllKeys
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.reader.JavaxImageReader
import io.github.shaksternano.borgar.core.media.reader.NoAudioReader

private val mediaReaderFactories: Map<String, ImageReaderFactory> = buildMap {
    registerFactory(JavaxImageReader.Factory)
}

private val audioReaderFactories: Map<String, AudioReaderFactory> = buildMap {

}

private fun <T : VideoFrame<*>> MutableMap<String, MediaReaderFactory<T>>.registerFactory(
    factory: MediaReaderFactory<T>,
) = putAllKeys(
    factory.supportedFormats,
    factory,
)

fun createImageReader(input: DataSource, format: String): ImageReader {
    val factory = mediaReaderFactories.getOrDefault(format, JavaxImageReader.Factory)
    return factory.create(input)
}

fun createAudioReader(input: DataSource, format: String): AudioReader {
    val factory = audioReaderFactories.getOrDefault(format, NoAudioReader.Factory)
    return factory.create(input)
}
