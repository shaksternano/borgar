package io.github.shaksternano.borgar.core.io

import com.google.common.io.Closer
import com.google.common.io.Files
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.Closeable
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.appendBytes
import kotlin.io.path.createTempFile
import kotlin.io.use

suspend fun createTemporaryFile(fileName: String): Path {
    return createTemporaryFile(nameWithoutExtension(fileName), fileExtension(fileName))
}

suspend fun createTemporaryFile(nameWithoutExtension: String, extension: String): Path {
    val extensionWithDot = if (extension.isBlank()) "" else ".$extension"
    val path = withContext(Dispatchers.IO) {
        createTempFile(nameWithoutExtension, extensionWithDot)
    }
    path.toFile().deleteOnExit()
    return path
}

suspend fun HttpResponse.download(path: Path) {
    val channel = bodyAsChannel()
    while (!channel.isClosedForRead) {
        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
        while (!packet.isEmpty) {
            val bytes = packet.readBytes()
            withContext(Dispatchers.IO) {
                path.appendBytes(bytes)
            }
        }
    }
}

suspend fun Path.write(inputStream: InputStream) {
    withContext(Dispatchers.IO) {
        FileUtils.copyInputStreamToFile(inputStream, toFile())
    }
}

fun removeQueryParams(url: String): String {
    return url.split('?').first()
}

fun filename(url: String): String {
    return Files.getNameWithoutExtension(removeQueryParams(url))
}

fun filename(nameWithoutExtension: String, extension: String): String {
    val extensionWithDot = if (extension.isBlank()) "" else ".$extension"
    return nameWithoutExtension + extensionWithDot
}

fun nameWithoutExtension(fileName: String): String {
    return Files.getNameWithoutExtension(removeQueryParams(fileName))
}

fun fileExtension(fileName: String): String {
    return Files.getFileExtension(removeQueryParams(fileName))
}

fun closeAll(vararg closeables: Closeable?) {
    closeAll(closeables.asIterable())
}

fun closeAll(closeables: Iterable<Closeable?>) {
    Closer.create().use {
        for (closeable in closeables) {
            it.register(closeable)
        }
    }
}

inline fun <A : Closeable?, B : Closeable?, R> useAll(closeable1: A, closeable2: B, block: (A, B) -> R): R {
    return closeable1.use {
        closeable2.use {
            block(closeable1, closeable2)
        }
    }
}

inline fun <A : Closeable?, B : Closeable?, C : Closeable?, R> useAll(
    closeable1: A,
    closeable2: B,
    closeable3: C,
    block: (A, B, C) -> R
): R {
    return closeable1.use {
        closeable2.use {
            closeable3.use {
                block(closeable1, closeable2, closeable3)
            }
        }
    }
}
