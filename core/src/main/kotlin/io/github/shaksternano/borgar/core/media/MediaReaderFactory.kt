package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.reader.MediaReader

interface MediaReaderFactory<T : VideoFrame<*>> {

    val supportedFormats: Set<String>

    suspend fun create(input: DataSource): MediaReader<T>
}

interface ImageReaderFactory : MediaReaderFactory<ImageFrame> {
    override suspend fun create(input: DataSource): ImageReader
}

interface AudioReaderFactory : MediaReaderFactory<AudioFrame> {
    override suspend fun create(input: DataSource): AudioReader
}
