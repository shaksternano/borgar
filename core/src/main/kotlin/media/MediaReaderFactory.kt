package com.shakster.borgar.core.media

import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.media.reader.AudioReader
import com.shakster.borgar.core.media.reader.ImageReader
import com.shakster.borgar.core.media.reader.MediaReader

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
