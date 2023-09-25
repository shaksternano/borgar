package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.collect.putAllKeys
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.reader.*

private val mediaReaderFactories: Map<String, ImageReaderFactory> = buildMap {
    registerFactory(JavaxImageReader.Factory)
    registerFactory(ScrimageGifReader.Factory)
    registerFactory(WebPImageReader.Factory)
}

private val audioReaderFactories: Map<String, AudioReaderFactory> = buildMap {

}

private fun MutableMap<String, ImageReaderFactory>.registerFactory(
    factory: ImageReaderFactory,
) = putAllKeys(
    factory.supportedFormats,
    factory,
)

private fun MutableMap<String, AudioReaderFactory>.registerFactory(
    factory: AudioReaderFactory,
) = putAllKeys(
    factory.supportedFormats,
    factory,
)

suspend fun createImageReader(input: DataSource, format: String): ImageReader {
    val factory = mediaReaderFactories.getOrDefault(format, JavaxImageReader.Factory)
    return factory.create(input)
}

suspend fun createAudioReader(input: DataSource, format: String): AudioReader {
    val factory = audioReaderFactories.getOrDefault(format, NoAudioReader.Factory)
    return factory.create(input)
}
