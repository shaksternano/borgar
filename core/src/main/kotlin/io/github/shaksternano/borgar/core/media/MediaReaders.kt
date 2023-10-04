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

private fun <T : MediaReaderFactory<*>> MutableMap<String, T>.registerFactory(
    factory: T,
) = putAllKeys(
    factory.supportedFormats,
    factory,
)

suspend fun createImageReader(input: DataSource, format: String): ImageReader {
    val factory = mediaReaderFactories.getOrDefault(format, FFmpegImageReader.Factory)
    return factory.create(input)
}

suspend fun createAudioReader(input: DataSource, format: String): AudioReader {
    val factory = audioReaderFactories.getOrDefault(format, FFmpegAudioReader.Factory)
    return factory.create(input)
}
