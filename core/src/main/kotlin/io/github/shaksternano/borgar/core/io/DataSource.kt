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

interface DataSource {

    val filename: String
    val path: Path?
    val url: String?

    suspend fun newStream(): InputStream

    fun newStreamBlocking(): InputStream = runBlocking {
        newStream()
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
        return fromFile(filename, path)
    }

    suspend fun writeToPath(path: Path) {
        url?.let {
            download(it, path)
        } ?: path.write(newStream())
    }

    companion object {
        fun fromFile(name: String? = null, path: Path): FileDataSource {
            val filename = name ?: path.fileName?.toString() ?: throw IllegalArgumentException("Invalid path")
            return FileDataSource(filename, path)
        }

        fun fromUrl(name: String? = null, url: String): UrlDataSource {
            val filename = name ?: filename(url)
            return UrlDataSource(filename, url)
        }

        fun fromBytes(name: String, bytes: ByteArray): DataSource = BytesDataSource(name, bytes)

        fun fromStreamSupplier(name: String, streamSupplier: suspend () -> InputStream): DataSource {
            return object : DataSource {
                override val filename: String = name
                override val path: Path? = null
                override val url: String? = null

                override suspend fun newStream(): InputStream = streamSupplier()
            }
        }
    }
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

    override suspend fun size(): Long = withContext(Dispatchers.IO) {
        path.fileSize()
    }
}

data class UrlDataSource(
    override val filename: String,
    override val url: String,
) : DataSource {

    override val path: Path? = null

    override suspend fun newStream(): InputStream {
        return useHttpClient { client ->
            val response = client.get(url)
            response.body<InputStream>()
        }
    }

    override suspend fun size(): Long {
        return useHttpClient { client ->
            val response = client.get(url)
            response.size()
        }
    }
}

private class BytesDataSource(
    override val filename: String,
    val bytes: ByteArray,
) : DataSource {

    override val path: Path? = null
    override val url: String? = null

    override suspend fun newStream(): InputStream = bytes.inputStream()

    override fun newStreamBlocking(): InputStream = bytes.inputStream()

    override suspend fun size(): Long = bytes.size.toLong()

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
        "BytesDataSource(filename=$filename, bytes=${bytes.contentToString()})"
}
