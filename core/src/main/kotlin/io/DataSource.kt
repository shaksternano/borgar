package io.github.shaksternano.borgar.core.io

import io.github.shaksternano.borgar.core.exception.FileTooLargeException
import io.github.shaksternano.borgar.core.exception.UnreadableFileException
import io.github.shaksternano.borgar.core.util.hash
import io.github.shaksternano.borgar.core.util.kClass
import io.ktor.http.*
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
    val sendUrl: Boolean

    suspend fun newStream(): InputStream

    fun newStreamBlocking(): InputStream = runBlocking {
        newStream()
    }

    suspend fun toByteArray(): ByteArray = newStream().use {
        withContext(IO_DISPATCHER) {
            it.readAllBytes()
        }
    }

    suspend fun size(): Long? = null

    suspend fun isWithinReportedSize(maxSize: Long): Boolean =
        size()?.let { it <= maxSize } != false

    suspend fun getOrWriteFile(
        limit: Long = 0,
    ): FileDataSource {
        if (this is FileDataSource) return this
        val path = path ?: let {
            val newPath = createTemporaryFile(filename)
            runCatching {
                writeToPath(newPath, limit)
            }.onFailure {
                newPath.deleteSilently()
                throw it as? FileTooLargeException ?: UnreadableFileException(it)
            }
            newPath
        }
        return fromFile(path, filename, url)
    }

    private suspend fun writeToPath(
        path: Path,
        limit: Long = 0,
    ) {
        url?.let {
            download(it, path, limit)
        } ?: path.write(newStream(), limit)
    }

    fun rename(newName: String): DataSource = object : DataSource {
        override val filename: String = newName
        override val path: Path? = this@DataSource.path
        override val url: String? = this@DataSource.url
        override val sendUrl: Boolean = false

        override suspend fun newStream(): InputStream = this@DataSource.newStream()
    }

    fun withSendUrl(sendUrl: Boolean): DataSource =
        if (url == null) this
        else object : DataSource {
            override val filename: String = this@DataSource.filename
            override val path: Path? = this@DataSource.path
            override val url: String? = this@DataSource.url
            override val sendUrl: Boolean = sendUrl

            override suspend fun newStream(): InputStream = this@DataSource.newStream()
        }

    override fun asDataSource(): DataSource = this

    companion object {
        fun fromFile(path: Path, filename: String = path.filename, url: String? = null): FileDataSource =
            FileDataSource(filename, path, url)

        fun fromUrl(url: String, filename: String = filename(url), sendUrl: Boolean = false): UrlDataSource =
            UrlDataSource(filename, url, sendUrl)

        fun fromBytes(filename: String, bytes: ByteArray): DataSource =
            BytesDataSource(filename, bytes)

        fun fromStreamSupplier(filename: String, streamSupplier: suspend () -> InputStream): DataSource =
            StreamSupplierDataSource(filename, streamSupplier)

        fun fromResource(path: String): DataSource = fromStreamSupplier(filename(path)) {
            getResource(path)
        }
    }
}

fun interface DataSourceConvertable {

    fun asDataSource(): DataSource
}

data class FileDataSource(
    override val filename: String,
    override val path: Path,
    override val url: String? = null,
) : DataSource {

    override val sendUrl: Boolean = false
    private var size: Long? = null

    override suspend fun newStream(): InputStream = path.inputStreamSuspend()

    override fun newStreamBlocking(): InputStream = path.inputStream()

    override suspend fun toByteArray(): ByteArray = withContext(IO_DISPATCHER) {
        path.readBytes()
    }

    override suspend fun size(): Long {
        size?.let {
            return it
        }
        return withContext(IO_DISPATCHER) {
            path.fileSize()
        }.also {
            size = it
        }
    }

    override fun rename(newName: String): FileDataSource = copy(filename = newName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false
        other as FileDataSource
        if (filename != other.filename) return false
        if (path != other.path) return false
        if (url != other.url) return false
        return true
    }

    override fun hashCode(): Int = hash(
        filename,
        path,
        url,
    )

    override fun toString(): String {
        return "FileDataSource(filename='$filename', path=$path)"
    }
}

data class UrlDataSource(
    override val filename: String,
    override val url: String,
    override val sendUrl: Boolean = false,
) : DataSource {

    override val path: Path? = null
    private var size: Long? = null
    private var setSize: Boolean = false

    override suspend fun newStream(): InputStream =
        httpGet<InputStream>(url)

    override suspend fun toByteArray(): ByteArray =
        httpGet<ByteArray>(url)

    override suspend fun size(): Long? {
        if (setSize) {
            return size
        }
        return useHttpClient { client ->
            val headResponse = client.head(url)
            headResponse.contentLength()
        }.also {
            size = it
            setSize = true
        }
    }

    override fun rename(newName: String): UrlDataSource = copy(
        filename = newName,
        sendUrl = false,
    )

    override fun withSendUrl(sendUrl: Boolean): DataSource = copy(sendUrl = sendUrl)

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
        return "UrlDataSource(filename='$filename', url='$url', sendUrl=$sendUrl)"
    }
}

private data class BytesDataSource(
    override val filename: String,
    val bytes: ByteArray,
) : DataSource {

    override val path: Path? = null
    override val url: String? = null
    override val sendUrl: Boolean = false

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
    override val sendUrl: Boolean = false

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
