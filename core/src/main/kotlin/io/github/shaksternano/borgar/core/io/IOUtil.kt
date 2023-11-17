package io.github.shaksternano.borgar.core.io

import com.google.common.io.Closer
import com.google.common.io.Files
import io.github.shaksternano.borgar.core.media.mediaFormat
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import java.io.Closeable
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.appendBytes
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.use

suspend fun createTemporaryFile(filename: String): Path =
    createTemporaryFile(filenameWithoutExtension(filename), fileExtension(filename))

suspend fun createTemporaryFile(filenameWithoutExtension: String, extension: String): Path {
    val extensionWithDot = if (extension.isBlank()) "" else ".$extension"
    val path = withContext(Dispatchers.IO) {
        createTempFile(filenameWithoutExtension, extensionWithDot)
    }
    path.toFile().deleteOnExit()
    return path
}

suspend fun Path.deleteSilently() {
    runCatching {
        withContext(Dispatchers.IO) {
            deleteIfExists()
        }
    }
}

fun configuredHttpClient(json: Boolean = false): HttpClient = HttpClient(Apache5) {
    if (json) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    install(HttpRequestRetry) {
        maxRetries = 3
        retryIf { _, response ->
            !response.status.isSuccess()
        }
        constantDelay(5000)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 60000
    }
}

inline fun <T> useHttpClient(json: Boolean = false, block: (HttpClient) -> T) =
    configuredHttpClient(json).use(block)

suspend inline fun <reified T> httpGet(url: String) = useHttpClient(true) {
    it.get(url).body<T>()
}

suspend fun download(url: String, path: Path) = useHttpClient { client ->
    val response = client.get(url)
    response.download(path)
}

suspend fun HttpResponse.download(path: Path) = readBytes {
    withContext(Dispatchers.IO) {
        path.appendBytes(it)
    }
}

suspend fun HttpResponse.size(): Long =
    headers["Content-Length"]?.toLongOrNull() ?: let {
        var size = 0L
        readBytes {
            size += it.size
        }
        size
    }

private suspend inline fun HttpResponse.readBytes(block: (ByteArray) -> Unit) {
    val channel = bodyAsChannel()
    while (!channel.isClosedForRead) {
        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
        while (!packet.isEmpty) {
            val bytes = packet.readBytes()
            block(bytes)
        }
    }
}

suspend fun Path.write(inputStream: InputStream) = withContext(Dispatchers.IO) {
    FileUtils.copyInputStreamToFile(inputStream, toFile())
}

suspend fun DataSource.fileFormat(): String {
    val mediaFormat = path?.let { mediaFormat(it) } ?: mediaFormat(newStream())
    return mediaFormat ?: fileExtension()
}

fun removeQueryParams(url: String): String =
    url.split('?').first()

fun filename(url: String): String {
    val noQueryParams = removeQueryParams(url)
    val nameWithoutExtension = Files.getNameWithoutExtension(noQueryParams)
    val extension = Files.getFileExtension(noQueryParams)
    return filename(nameWithoutExtension, extension)
}

fun filename(nameWithoutExtension: String, extension: String): String {
    val extensionWithDot = if (extension.isBlank()) "" else ".$extension"
    return nameWithoutExtension + extensionWithDot
}

fun filenameWithoutExtension(fileName: String): String =
    Files.getNameWithoutExtension(removeQueryParams(fileName))

fun fileExtension(fileName: String): String =
    Files.getFileExtension(removeQueryParams(fileName))

fun DataSource.filenameWithoutExtension(): String = filenameWithoutExtension(filename)

fun DataSource.fileExtension(): String = fileExtension(filename)

fun toMb(bytes: Long): Long =
    bytes / 1024 / 1024

fun closeAll(vararg closeables: Closeable?) =
    closeAll(closeables.asIterable())

fun closeAll(closeables: Iterable<Closeable?>) =
    Closer.create().use {
        for (closeable in closeables) {
            it.register(closeable)
        }
    }

inline fun <A : Closeable?, B : Closeable?, R> useAll(
    closeable1: A,
    closeable2: B,
    block: (A, B) -> R,
): R =
    closeable1.use {
        closeable2.use {
            block(closeable1, closeable2)
        }
    }

inline fun <A : Closeable?, B : Closeable?, C : Closeable?, R> useAll(
    closeable1: A,
    closeable2: B,
    closeable3: C,
    block: (A, B, C) -> R,
): R =
    closeable1.use {
        closeable2.use {
            closeable3.use {
                block(closeable1, closeable2, closeable3)
            }
        }
    }
