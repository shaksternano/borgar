package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.collect.parallelMap
import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.useHttpClient
import io.github.shaksternano.borgar.core.util.parseTags
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

abstract class ApiFilesTask(
    tags: String,
    private val fileCount: Int,
    private val filePrefix: String,
    private val maxFileSize: Long,
) : BaseFileTask() {

    override val requireInput: Boolean = false
    private val tags: Set<String> = parseTags(tags)

    override suspend fun run(input: List<DataSource>): List<DataSource> {
        val apiResponses = useHttpClient { client ->
            (1..fileCount).parallelMap {
                runCatching {
                    getApiResponse(client, tags)
                }.getOrElse {
                    return@parallelMap null
                }
            }
            .filterNotNull()
            .distinctBy {
                it.id
            }
        }
        return useHttpClient(json = false) { client ->
            apiResponses.parallelMap {
                val fileResponse = runCatching {
                    client.get(it.url)
                }.getOrElse {
                    return@parallelMap null
                }
                val contentLength = fileResponse.contentLength() ?: 0
                if (!fileResponse.status.isSuccess() || contentLength > maxFileSize) {
                    return@parallelMap null
                }
                val extension = fileResponse.contentType()
                    ?.contentSubtype
                    ?: it.extension
                val fixedExtension = if (extension.equals("jpeg", true)) {
                    "jpg"
                } else {
                    extension.lowercase()
                }
                val filename = "$filePrefix-${it.id}.${fixedExtension}"
                val bytes = fileResponse.readRawBytes()
                DataSource.fromBytes(filename, bytes)
            }
            .filterNotNull()
            .ifEmpty {
                throw ErrorResponseException("No images found!")
            }
        }
    }

    private suspend fun getApiResponse(
        client: HttpClient,
        tags: Set<String>
    ): ApiResponse {
        val url = getRequestUrl(tags)
        val response = client.get(url) {
            contentType(ContentType.Application.Json)
        }
        return if (response.status.isSuccess()) {
            parseResponse(response)
        } else {
            throw Exception("Bad response: ${response.status}")
        }
    }

    protected abstract fun getRequestUrl(tags: Set<String>): String

    protected abstract suspend fun parseResponse(response: HttpResponse): ApiResponse

    protected data class ApiResponse(
        val id: String,
        val url: String,
        val extension: String,
    )
}
