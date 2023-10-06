package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.collect.putAllKeys
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.reader.*

private val imageReaderFactories: MutableMap<String, ImageReaderFactory> = mutableMapOf()

private val audioReaderFactories: MutableMap<String, AudioReaderFactory> = mutableMapOf()

@Suppress("unused")
private val init = run {
    registerImageOnlyFactory(JavaxImageReader.Factory)
    registerImageOnlyFactory(ScrimageGifReader.Factory)
    registerImageOnlyFactory(WebPImageReader.Factory)
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

suspend fun createImageReader(input: DataSource, format: String): ImageReader {
    val factory = imageReaderFactories.getOrDefault(format, FFmpegImageReader.Factory)
    return factory.create(input)
}

suspend fun createAudioReader(input: DataSource, format: String): AudioReader {
    val factory = audioReaderFactories.getOrDefault(format, FFmpegAudioReader.Factory)
    return factory.create(input)
}
