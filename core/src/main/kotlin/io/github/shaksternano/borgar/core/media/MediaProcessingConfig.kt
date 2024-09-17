package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader

interface MediaProcessingConfig {

    val outputName: String

    suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader = imageReader

    suspend fun transformAudioReader(audioReader: AudioReader, outputFormat: String): AudioReader = audioReader

    fun transformOutputFormat(inputFormat: String): String = inputFormat

    infix fun then(after: MediaProcessingConfig): MediaProcessingConfig {
        return ChainedMediaProcessingConfig(this, after)
    }
}
