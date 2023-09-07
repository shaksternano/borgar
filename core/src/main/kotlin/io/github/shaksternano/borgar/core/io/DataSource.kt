package io.github.shaksternano.borgar.core.io

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

interface DataSource {

    val name: String
    val path: Path?
    val fromFile: Boolean
        get() = path != null

    suspend fun newStream(): InputStream

    suspend fun getOrWriteFile(): Path {
        val path = createTemporaryFile(name)
        path.write(newStream())
        return path
    }

    companion object {
        fun fromFile(name: String? = null, path: Path): DataSource {
            val filename = name ?: path.fileName?.toString() ?: "file"
            return object : DataSourceImpl(filename, path) {
                override suspend fun newStream(): InputStream = withContext(Dispatchers.IO) {
                    path.inputStream()
                }
            }
        }

        fun fromUrl(name: String? = null, url: String): DataSource {
            val filename = name ?: filename(url)
            return object : DataSourceImpl(filename, null) {
                override suspend fun newStream(): InputStream = HttpClient(CIO) {
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
                }.use { client ->
                    val response = client.get(url)
                    response.body<InputStream>()
                }
            }
        }

        fun fromBytes(name: String, bytes: ByteArray): DataSource {
            return object : DataSourceImpl(name, null) {
                override suspend fun newStream(): InputStream = bytes.inputStream()
            }
        }

        fun fromStreamSupplier(name: String, streamSupplier: suspend () -> InputStream): DataSource {
            return object : DataSourceImpl(name, null) {
                override suspend fun newStream(): InputStream = streamSupplier()
            }
        }
    }
}

private abstract class DataSourceImpl(
    override val name: String,
    override val path: Path?
) : DataSource
