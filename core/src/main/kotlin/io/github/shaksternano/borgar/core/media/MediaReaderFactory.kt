package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.reader.MediaReader

interface MediaReaderFactory<T : VideoFrame<*>> {

    val supportedFormats: Set<String>

    fun create(input: DataSource): MediaReader<T>
}

typealias ImageReaderFactory = MediaReaderFactory<ImageFrame>
typealias AudioReaderFactory = MediaReaderFactory<AudioFrame>
