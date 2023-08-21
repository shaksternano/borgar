package io.github.shaksternano.borgar.command

import com.google.common.collect.ListMultimap
import io.github.shaksternano.borgar.collect.parallelMap
import io.github.shaksternano.borgar.command.util.CommandResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.io.InputStream

abstract class ApiFilesCommand(
    name: String,
    description: String,
    private val count: Int?,
    private val prefix: String,
) : KotlinCommand<Unit>(name, description) {

    final override suspend fun executeSuspend(
        arguments: List<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): CommandResponse<Unit> {
        HttpClient(CIO) {
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
        }.use { client ->
            val fileCount = (count ?: arguments.firstOrNull()?.toIntOrNull()).let {
                if (it == null) {
                    1
                } else {
                    (1..Message.MAX_FILE_AMOUNT).bound(it)
                }
            }
            val files = (1..fileCount)
                .parallelMap {
                    try {
                        response(client)
                    } catch (e: Exception) {
                        return@parallelMap null
                    }
                }
                .filterNotNull()
                .distinctBy {
                    it.id
                }
                .parallelMap {
                    val fileResponse = try {
                        client.get(it.url)
                    } catch (e: Exception) {
                        return@parallelMap null
                    }
                    val inputStream = download(fileResponse) ?: return@parallelMap null
                    val extension = fileResponse.headers["Content-Type"]?.let { contentType ->
                        val extensionSplitIndex = contentType.lastIndexOf('/')
                        contentType.substring(extensionSplitIndex + 1)
                    } ?: it.extension
                    val fixedExtension = if (extension.equals("jpeg", true)) {
                        "jpg"
                    } else {
                        extension.lowercase()
                    }
                    val fileName = "$prefix-${it.id}.${fixedExtension}"
                    FileUpload.fromData(inputStream, fileName)
                }
                .filterNotNull()
            return if (files.isEmpty()) {
                CommandResponse("Error getting images!")
            } else {
                CommandResponse(MessageCreateData.fromFiles(files))
            }
        }
    }

    private fun IntRange.bound(value: Int): Int {
        return when {
            value < first -> first
            value > last -> last
            else -> value
        }
    }

    private suspend fun response(client: HttpClient): ApiResponse {
        val url = requestUrl()
        val response = client.get(url) {
            contentType(ContentType.Application.Json)
        }
        return parseResponse(response)
    }

    protected abstract fun requestUrl(): String

    protected abstract suspend fun parseResponse(response: HttpResponse): ApiResponse

    private suspend fun download(response: HttpResponse): InputStream? {
        val contentLength = response.headers["Content-Length"]?.toLongOrNull() ?: 0
        return if (response.status.isSuccess() && contentLength <= Message.MAX_FILE_SIZE) {
            response.body<InputStream>()
        } else {
            null
        }
    }

    protected data class ApiResponse(
        val id: String,
        val url: String,
        val extension: String,
    )
}
