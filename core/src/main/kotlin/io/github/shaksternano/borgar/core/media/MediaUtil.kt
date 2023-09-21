package io.github.shaksternano.borgar.core.media

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.file.Path
import javax.imageio.ImageIO

suspend fun mediaFormat(path: Path): String? = mediaFormatImpl(path.toFile())

suspend fun mediaFormat(inputStream: InputStream): String? {
    return inputStream.use {
        mediaFormatImpl(it)
    }
}

private suspend fun mediaFormatImpl(input: Any): String? {
    return withContext(Dispatchers.IO) {
        ImageIO.createImageInputStream(input).use {
            if (it != null) {
                val readers = ImageIO.getImageReaders(it)
                if (readers.hasNext()) {
                    return@use readers.next().formatName.lowercase()
                }
            }
            null
        }
    }
}
