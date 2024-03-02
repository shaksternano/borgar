package io.github.shaksternano.borgar.core.io

import com.google.common.io.Closer
import com.google.common.io.Files
import io.github.shaksternano.borgar.core.media.mediaFormat
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.jetty.*
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
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import java.io.Closeable
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.file.Path
import java.util.regex.Pattern
import kotlin.io.path.*
import kotlin.io.use

private object IOUtil

val ALLOWED_DOMAINS: Set<String> = setOf(
    "raw.githubusercontent.com",
    "cdn.discordapp.com",
    "media.discordapp.net",
    "autumn.revolt.chat",
    "pbs.twimg.com",
    "i.redd.it",
)

suspend fun createTemporaryFile(filename: String): Path = createTemporaryFile(
    filenameWithoutExtension(filename),
    fileExtension(filename),
)

suspend fun createTemporaryFile(filenameWithoutExtension: String, extension: String): Path {
    val extensionWithDot = if (extension.isBlank()) "" else ".$extension"
    val path = withContext(Dispatchers.IO) {
        createTempFile(filenameWithoutExtension, extensionWithDot)
    }
    path.toFile().deleteOnExit()
    return path
}

suspend fun getResource(resourcePath: String): InputStream =
    withContext(Dispatchers.IO) {
        IOUtil.javaClass.classLoader.getResourceAsStream(resourcePath)
    } ?: throw FileNotFoundException("Resource not found: $resourcePath")

suspend fun forEachResource(
    directory: String,
    operation: suspend (resourcePath: String, inputStream: InputStream) -> Unit
) {
    // Remove trailing forward slashes
    val trimmedDirectory = directory.trim { it <= ' ' }.replace("/$".toRegex(), "")
    val packageName = trimmedDirectory.replace(Pattern.quote("/").toRegex(), ".")
    getResourcePaths(packageName).forEach { resourcePath ->
        getResource(resourcePath).use { inputStream ->
            operation(resourcePath, inputStream)
        }
    }
}

private suspend fun getResourcePaths(packageName: String): Set<String> = withContext(Dispatchers.IO) {
    val reflections = Reflections(packageName, Scanners.Resources)
    reflections.getResources("(.*?)")
}

val Path.filename: String
    get() = fileName?.toString() ?: throw IllegalArgumentException("Invalid path")

suspend fun Path.deleteSilently() {
    runCatching {
        withContext(Dispatchers.IO) {
            @OptIn(ExperimentalPathApi::class)
            deleteRecursively()
        }
    }
}

suspend fun Path.clear() =
    withContext(Dispatchers.IO) {
        RandomAccessFile(toFile(), "rw").use { file ->
            file.setLength(0)
        }
    }

fun httpClient(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient = HttpClient(Jetty, block)

fun configuredHttpClient(): HttpClient = httpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
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

inline fun <T> useHttpClient(block: (HttpClient) -> T): T =
    configuredHttpClient().use(block)

suspend inline fun <reified T> httpGet(url: String): T = useHttpClient {
    it.get(url).body<T>()
}

suspend fun download(url: String, path: Path) = useHttpClient { client ->
    val response = client.get(url)
    response.download(path)
}

suspend fun HttpResponse.download(path: Path) {
    path.clear()
    readBytes {
        withContext(Dispatchers.IO) {
            path.appendBytes(it)
        }
    }
}

suspend fun HttpResponse.size(): Long =
    contentLength() ?: let {
        var size = 0L
        readBytes {
            size += it.size
        }
        size
    }

fun HttpResponse.filename(): String? = headers["Content-Disposition"]?.let {
    val headerParts = it.split("filename=", limit = 2)
    if (headerParts.size == 2) {
        val filename = headerParts[1].trim()
            .removeSurrounding("\"")
        filename.ifBlank { null }
    } else null
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

suspend fun Path.inputStreamSuspend(): InputStream = withContext(Dispatchers.IO) {
    inputStream()
}

suspend fun DataSource.fileFormat(): String {
    val mediaFormat = path?.let { mediaFormat(it) } ?: mediaFormat(newStream())
    return mediaFormat ?: fileExtension()
}

fun removeQueryParams(url: String): String =
    url.split('?').first()

fun filename(filePath: String): String {
    val noQueryParams = removeQueryParams(filePath)
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

suspend fun InputStream.readSuspend(): Int = withContext(Dispatchers.IO) {
    read()
}

suspend fun InputStream.readNBytesSuspend(n: Int): ByteArray = withContext(Dispatchers.IO) {
    readNBytes(n)
}

suspend fun InputStream.skipNBytesSuspend(n: Long) = withContext(Dispatchers.IO) {
    skipNBytes(n)
}
