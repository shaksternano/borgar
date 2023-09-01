package io.github.shaksternano.borgar.command

import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

private const val CAT_API_DOMAIN = "https://cataas.com"

class CatCommand(
    name: String,
    description: String,
    count: Int? = null,
) : ApiFilesCommand(
    name,
    description,
    count,
    "cat",
) {

    override fun requestUrl(tags: List<String>): String {
        val isGif = Random.nextInt(5) == 0
        val path = if (isGif) "/cat/gif" else "/cat"
        return "$CAT_API_DOMAIN$path?json=true"
    }

    override suspend fun parseResponse(response: HttpResponse): ApiResponse {
        val body = response.body<ResponseBody>()
        val url = "$CAT_API_DOMAIN${body.url}"
        val extensionSplitIndex = body.mimetype.lastIndexOf('/')
        val extension = body.mimetype.substring(extensionSplitIndex + 1)
        return ApiResponse(
            body.id,
            url,
            extension,
        )
    }

    @Serializable
    private data class ResponseBody(
        @SerialName("_id")
        val id: String,
        val mimetype: String,
        val url: String,
    )
}
