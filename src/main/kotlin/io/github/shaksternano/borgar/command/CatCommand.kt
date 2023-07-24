package io.github.shaksternano.borgar.command

import com.google.common.collect.ListMultimap
import io.github.shaksternano.borgar.command.util.CommandResponse
import io.github.shaksternano.borgar.util.collect.parallelMap
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.io.InputStream
import java.net.URL
import kotlin.random.Random

private const val CAT_API_DOMAIN = "https://cataas.com"

class CatCommand(
    name: String,
    description: String,
    private val count: Int,
) : KotlinCommand<Unit>(name, description) {

    override suspend fun executeSuspend(
        arguments: List<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): CommandResponse<Unit> {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        val files = (1..count)
            .parallelMap { catResponse(client) }
            .distinctBy { it.id }
            .parallelMap {
                val url = CAT_API_DOMAIN + it.url
                val inputStream = runCatching {
                    download(url)
                }.getOrElse {
                    return@parallelMap null
                }
                val extensionSplitIndex = it.mimetype.lastIndexOf('/')
                val extension = it.mimetype
                    .substring(extensionSplitIndex + 1)
                    .lowercase()
                    .let { ext ->
                        if (ext == "jpeg") {
                            "jpg"
                        } else {
                            ext
                        }
                    }
                val fileName = "cat-${it.id}.$extension"
                FileUpload.fromData(inputStream, fileName)
            }
            .filterNotNull()
        return CommandResponse(MessageCreateData.fromFiles(files))
    }

    private suspend fun catResponse(client: HttpClient): CatResponseBody {
        val isGif = Random.nextBoolean()
        val path = if (isGif) "/cat/gif" else "/cat"
        val url = CAT_API_DOMAIN + path + "?json=true"
        return client.get(url) {
            contentType(ContentType.Application.Json)
        }.body<CatResponseBody>()
    }

    private suspend fun download(url: String): InputStream {
        return withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection()
            connection.readTimeout = 10000
            connection.inputStream
        }
    }
}

@Serializable
private data class CatResponseBody(
    @SerialName("_id")
    val id: String,
    val mimetype: String,
    val url: String,
)
