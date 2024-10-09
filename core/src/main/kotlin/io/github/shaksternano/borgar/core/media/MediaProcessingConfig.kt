package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader

interface MediaProcessingConfig {

    val outputName: String
        get() = ""
    val outputExtension: String
        get() = ""

    suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader = imageReader

    suspend fun transformAudioReader(audioReader: AudioReader, outputFormat: String): AudioReader = audioReader

    fun transformOutputFormat(inputFormat: String): String = inputFormat

    infix fun then(after: MediaProcessingConfig): MediaProcessingConfig {
        return ChainedMediaProcessingConfig(this, after)
    }
}

private class ChainedMediaProcessingConfig(
    private val first: MediaProcessingConfig,
    private val second: MediaProcessingConfig,
) : MediaProcessingConfig {

    override val outputName: String = second.outputName.ifBlank {
        first.outputName
    }
    override val outputExtension: String = second.outputExtension.ifBlank {
        first.outputExtension
    }

    override suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader {
        val firstReader = first.transformImageReader(imageReader, outputFormat)
        return second.transformImageReader(firstReader, outputFormat)
    }

    override suspend fun transformAudioReader(audioReader: AudioReader, outputFormat: String): AudioReader {
        val firstReader = first.transformAudioReader(audioReader, outputFormat)
        return second.transformAudioReader(firstReader, outputFormat)
    }

    override fun transformOutputFormat(inputFormat: String): String {
        val firstFormat = first.transformOutputFormat(inputFormat)
        return second.transformOutputFormat(firstFormat)
    }

    override fun toString(): String {
        return "ChainedMediaProcessingConfig(first=$first, second=$second, outputName='$outputName')"
    }
}
