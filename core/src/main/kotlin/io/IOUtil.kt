package com.shakster.borgar.core.io

import com.google.common.io.Closer
import com.shakster.borgar.core.exception.FileTooLargeException
import com.shakster.borgar.core.media.mediaFormat
import com.shakster.borgar.core.util.JSON
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.collections.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.io.readByteArray
import org.apache.commons.io.input.BoundedInputStream
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import java.io.Closeable
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.concurrent.Executors
import java.util.regex.Pattern
import kotlin.io.path.*
import com.google.common.io.Files as GuavaFiles

val IO_DISPATCHER: CoroutineDispatcher = Executors.newVirtualThreadPerTaskExecutor()
    .asCoroutineDispatcher()

val ALLOWED_HOSTS: Set<String> = setOf(
    "raw.githubusercontent.com",
    "cdn.discordapp.com",
    "media.discordapp.net",
    "cdn.revoltusercontent.com",
    "autumn.revolt.chat",
    "jan.revolt.chat",
    "pbs.twimg.com",
    "i.redd.it",
)

private val INVALID_FILENAME_CHARACTERS: Regex = "[^a-zA-Z0-9.,\\-_() ]".toRegex()

suspend fun createTemporaryFile(filename: String): Path = createTemporaryFile(
    filenameWithoutExtension(filename),
    fileExtension(filename),
)

suspend fun createTemporaryFile(filenameWithoutExtension: String, extension: String): Path {
    val extensionWithDot = if (extension.isBlank()) "" else ".$extension"
    val path = withContext(IO_DISPATCHER) {
        createTempFile(filenameWithoutExtension, extensionWithDot)
    }
    IOUtil.deleteOnExit(path)
    return path
}

suspend fun getResource(resourcePath: String): InputStream =
    withContext(IO_DISPATCHER) {
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

private suspend fun getResourcePaths(packageName: String): Set<String> = withContext(IO_DISPATCHER) {
    val reflections = Reflections(packageName, Scanners.Resources)
    reflections.getResources("(.*?)")
}

val Path.filename: String
    get() = fileName?.toString() ?: throw IllegalStateException("$this has no filename")

suspend fun Path.deleteSilently() {
    runCatching {
        withContext(IO_DISPATCHER) {
            @OptIn(ExperimentalPathApi::class)
            deleteRecursively()
        }
        IOUtil.removeDeleteOnExit(this)
    }
}

fun httpClient(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient = HttpClient(CIO, block)

fun configuredHttpClient(json: Boolean = true): HttpClient = httpClient {
    if (json) {
        install(ContentNegotiation) {
            json(JSON)
        }
    }
    install(HttpRequestRetry) {
        maxRetries = 3
        retryIf { _, response ->
            response.status == HttpStatusCode.TooManyRequests
                || response.status == HttpStatusCode.BadGateway
        }
        constantDelay(5000)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 60000
    }
}

inline fun <T> useHttpClient(json: Boolean = true, block: (HttpClient) -> T): T =
    configuredHttpClient(json).use(block)

inline fun <T> HttpResponse.ifSuccessful(block: (HttpResponse) -> T): T =
    if (status.isSuccess()) {
        block(this)
    } else {
        throw IOException("HTTP request failed: $status")
    }

suspend inline fun <reified T> httpGet(url: String): T = useHttpClient { client ->
    client.get(url).ifSuccessful {
        it.body<T>()
    }
}

suspend fun download(
    url: String,
    path: Path,
    limit: Long = 0,
) = useHttpClient { client ->
    client.get(url).ifSuccessful {
        it.download(path, limit)
    }
}

suspend fun HttpResponse.download(
    path: Path,
    limit: Long = 0,
) {
    var created = false
    var bytesRead = 0L
    readBytes {
        withContext(IO_DISPATCHER) {
            if (created) {
                path.writeBytes(
                    it,
                    StandardOpenOption.APPEND,
                )
            } else {
                path.writeBytes(
                    it,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE,
                )
                created = true
            }
        }
        if (limit > 0) {
            bytesRead += it.size
            if (bytesRead > limit) {
                throw FileTooLargeException()
            }
        }
    }
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
        val bytes = packet.readByteArray()
        block(bytes)
    }
}

suspend fun Path.write(
    inputStream: InputStream,
    limit: Long = 0,
) = withContext(IO_DISPATCHER) {
    inputStream.use {
        val bounded = limit > 0
        val boundedInputStream = if (bounded) {
            BoundedInputStream.builder()
                .setInputStream(it)
                .setMaxCount(limit)
                .setPropagateClose(false)
                .get()
        } else it
        Files.copy(boundedInputStream, this@write, StandardCopyOption.REPLACE_EXISTING)
        if (bounded && boundedInputStream.read() != -1) {
            throw FileTooLargeException()
        }
    }
}

suspend fun Path.inputStreamSuspend(): InputStream = withContext(IO_DISPATCHER) {
    inputStream()
}

suspend fun DataSource.fileFormat(): String {
    val mediaFormat = runCatching {
        path?.let { mediaFormat(it) } ?: mediaFormat(newStream())
    }.getOrNull()
    return (mediaFormat ?: fileExtension).lowercase()
}

suspend fun DataSource.toChannelProvider(): ChannelProvider =
    ChannelProvider(size()) {
        val url = url
        if (path == null && url != null) {
            HttpByteReadChannel(url)
        } else {
            LazyInitByteReadChannel {
                newStream().toByteReadChannel()
            }
        }
    }

fun removeQueryParams(url: String): String =
    url.split('?').first()

fun filename(filePath: String): String {
    val nameWithoutExtension = filenameWithoutExtension(filePath)
    val extension = fileExtension(filePath)
    return filename(nameWithoutExtension, extension)
}

fun filename(nameWithoutExtension: String, extension: String): String {
    val extensionWithDot = if (extension.isBlank()) "" else ".$extension"
    return nameWithoutExtension + extensionWithDot
}

fun filenameWithoutExtension(fileName: String): String =
    GuavaFiles.getNameWithoutExtension(removeQueryParams(fileName))

fun fileExtension(fileName: String): String =
    GuavaFiles.getFileExtension(removeQueryParams(fileName))

val DataSource.filenameWithoutExtension: String
    get() = filenameWithoutExtension(filename)

val DataSource.fileExtension: String
    get() = fileExtension(filename)

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

suspend fun InputStream.readSuspend(): Int = withContext(IO_DISPATCHER) {
    read()
}

suspend fun InputStream.readNBytesSuspend(n: Int): ByteArray = withContext(IO_DISPATCHER) {
    readNBytes(n)
}

suspend fun InputStream.skipNBytesSuspend(n: Long) = withContext(IO_DISPATCHER) {
    skipNBytes(n)
}

suspend inline fun <R> DataSource.useFile(block: (FileDataSource) -> R): R {
    val isInputTemp = path == null
    val fileInput = getOrWriteFile()
    return try {
        block(fileInput)
    } finally {
        if (isInputTemp) {
            fileInput.path.deleteSilently()
        }
    }
}

fun String.replaceInvalidFilenameCharacters(): String =
    replace(INVALID_FILENAME_CHARACTERS, "_")

private object IOUtil {

    private val toDelete: MutableSet<Path> = ConcurrentSet()

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            toDelete.forEach {
                runCatching {
                    @OptIn(ExperimentalPathApi::class)
                    it.deleteRecursively()
                }
            }
        })
    }

    fun deleteOnExit(path: Path) {
        toDelete.add(path)
    }

    fun removeDeleteOnExit(path: Path) {
        toDelete.remove(path)
    }
}
