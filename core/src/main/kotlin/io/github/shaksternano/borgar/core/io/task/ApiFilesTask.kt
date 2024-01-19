package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.collect.parallelMap
import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.useHttpClient
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

abstract class ApiFilesTask(
    tags: String,
    private val fileCount: Int,
    private val filePrefix: String,
    private val maxFileSize: Long,
) : BaseFileTask(
    requireInput = false,
) {

    private val tags: Set<String> = parseTags(tags)

    override suspend fun run(input: List<DataSource>): List<DataSource> = useHttpClient { client ->
        val files = (1..fileCount)
            .parallelMap {
                runCatching {
                    response(client, tags)
                }.getOrElse {
                    return@parallelMap null
                }
            }
            .filterNotNull()
            .distinctBy {
                it.id
            }
            .parallelMap {
                val response = runCatching {
                    client.get(it.url)
                }.getOrElse {
                    return@parallelMap null
                }
                val contentLength = response.contentLength() ?: 0
                if (!response.status.isSuccess() || contentLength > maxFileSize) {
                    return@parallelMap null
                }
                val extension = response.contentType()
                    ?.contentSubtype
                    ?: it.extension
                val fixedExtension = if (extension.equals("jpeg", true)) {
                    "jpg"
                } else {
                    extension.lowercase()
                }
                val filename = "$filePrefix-${it.id}.${fixedExtension}"
                val bytes = response.readBytes()
                DataSource.fromBytes(filename, bytes)
            }
            .filterNotNull()
        files.ifEmpty {
            throw ErrorResponseException("No images found!")
        }
    }

    private fun parseTags(tags: String): Set<String> {
        return tags
            .split(',')
            .map {
                it.trim()
            }
            .filter {
                it.isNotEmpty()
            }
            .toSet()
    }

    private suspend fun response(client: HttpClient, tags: Set<String>): ApiResponse {
        val url = requestUrl(tags)
        val response = client.get(url) {
            contentType(ContentType.Application.Json)
        }
        return if (response.status.isSuccess()) {
            parseResponse(response)
        } else {
            throw Exception("Bad response: ${response.status}")
        }
    }

    protected abstract fun requestUrl(tags: Set<String>): String

    protected abstract suspend fun parseResponse(response: HttpResponse): ApiResponse

    protected data class ApiResponse(
        val id: String,
        val url: String,
        val extension: String,
    )
}
