package io.github.shaksternano.borgar.core.io

import io.github.shaksternano.borgar.core.util.hash
import io.github.shaksternano.borgar.core.util.kClass
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.readBytes

interface DataSource : DataSourceConvertable {

    val filename: String
    val path: Path?
    val url: String?

    suspend fun newStream(): InputStream

    fun newStreamBlocking(): InputStream = runBlocking {
        newStream()
    }

    suspend fun toByteArray(): ByteArray = newStream().use {
        withContext(Dispatchers.IO) {
            it.readAllBytes()
        }
    }

    suspend fun size(): Long = withContext(Dispatchers.IO) {
        newStream().buffered().use {
            var size = 0L
            it.iterator().forEach { _ ->
                size++
            }
            size
        }
    }

    suspend fun getOrWriteFile(): FileDataSource {
        if (this is FileDataSource) return this
        val path = path ?: let {
            val newPath = createTemporaryFile(filename)
            writeToPath(newPath)
            newPath
        }
        return fromFile(path, filename)
    }

    suspend fun writeToPath(path: Path) {
        url?.let {
            download(it, path)
        } ?: path.write(newStream())
    }

    fun rename(newName: String): DataSource = object : DataSource {
        override val filename: String = newName
        override val path: Path? = this@DataSource.path
        override val url: String? = this@DataSource.url

        override suspend fun newStream(): InputStream = this@DataSource.newStream()
    }

    override fun asDataSource(): DataSource = this

    companion object {
        fun fromFile(path: Path, name: String? = null): FileDataSource {
            val filename = name ?: path.fileName?.toString() ?: throw IllegalArgumentException("Invalid path")
            return FileDataSource(filename, path)
        }

        fun fromUrl(url: String, name: String? = null): UrlDataSource {
            val filename = name ?: filename(url)
            return UrlDataSource(filename, url)
        }

        fun fromBytes(bytes: ByteArray, name: String): DataSource =
            BytesDataSource(name, bytes)

        fun fromStreamSupplier(name: String, streamSupplier: suspend () -> InputStream): DataSource =
            StreamSupplierDataSource(name, streamSupplier)
    }
}

fun interface DataSourceConvertable {

    fun asDataSource(): DataSource
}

data class FileDataSource(
    override val filename: String,
    override val path: Path,
) : DataSource {

    override val url: String? = null

    override suspend fun newStream(): InputStream = withContext(Dispatchers.IO) {
        path.inputStream()
    }

    override fun newStreamBlocking(): InputStream = path.inputStream()

    override suspend fun toByteArray(): ByteArray = withContext(Dispatchers.IO) {
        path.readBytes()
    }

    override suspend fun size(): Long = withContext(Dispatchers.IO) {
        path.fileSize()
    }

    override fun rename(newName: String): FileDataSource = copy(filename = newName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false
        other as FileDataSource
        if (filename != other.filename) return false
        if (path != other.path) return false
        return true
    }

    override fun hashCode(): Int = hash(
        filename,
        path,
    )

    override fun toString(): String {
        return "FileDataSource(filename='$filename', path=$path)"
    }
}

data class UrlDataSource(
    override val filename: String,
    override val url: String,
) : DataSource {

    override val path: Path? = null

    override suspend fun newStream(): InputStream = useHttpClient { client ->
        val response = client.get(url)
        response.body<InputStream>()
    }

    override suspend fun toByteArray(): ByteArray = useHttpClient { client ->
        val response = client.get(url)
        response.body<ByteArray>()
    }

    override suspend fun size(): Long = useHttpClient { client ->
        val response = client.get(url)
        response.size()
    }

    override fun rename(newName: String): UrlDataSource = copy(filename = newName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false
        other as UrlDataSource
        if (filename != other.filename) return false
        if (url != other.url) return false
        return true
    }

    override fun hashCode(): Int = hash(
        filename,
        url,
    )

    override fun toString(): String {
        return "UrlDataSource(filename='$filename', url='$url')"
    }
}

private data class BytesDataSource(
    override val filename: String,
    val bytes: ByteArray,
) : DataSource {

    override val path: Path? = null
    override val url: String? = null

    override suspend fun newStream(): InputStream = bytes.inputStream()

    override fun newStreamBlocking(): InputStream = bytes.inputStream()

    override suspend fun toByteArray(): ByteArray = bytes

    override suspend fun size(): Long = bytes.size.toLong()

    override fun rename(newName: String): DataSource = copy(filename = newName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false
        other as BytesDataSource
        if (filename != other.filename) return false
        if (!bytes.contentEquals(other.bytes)) return false
        return true
    }

    override fun hashCode(): Int = hash(
        filename,
        bytes.contentHashCode()
    )

    override fun toString(): String =
        "BytesDataSource(filename='$filename', bytes=${bytes.contentToString()})"
}

private data class StreamSupplierDataSource(
    override val filename: String,
    private val streamSupplier: suspend () -> InputStream,
) : DataSource {
    override val path: Path? = null
    override val url: String? = null

    override suspend fun newStream(): InputStream = streamSupplier()

    override fun rename(newName: String): DataSource = copy(filename = newName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false
        other as StreamSupplierDataSource
        if (filename != other.filename) return false
        if (streamSupplier != other.streamSupplier) return false
        return true
    }

    override fun hashCode(): Int = hash(
        filename,
        streamSupplier,
    )

    override fun toString(): String {
        return "StreamSupplierDataSource(filename='$filename', streamSupplier=$streamSupplier)"
    }
}
