package io.github.shaksternano.borgar.core.io.task

import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

private const val CAT_API_DOMAIN: String = "https://cataas.com"
private const val GIF_CHANCE: Double = 0.2

class CatTask(
    tags: String,
    fileCount: Int,
    maxFileSize: Long,
) : ApiFilesTask(
    tags,
    fileCount,
    filePrefix = "cat",
    maxFileSize,
) {

    override fun requestUrl(tags: Set<String>): String {
        val isGif =
            if (tags.equalsAnyIgnoreCase("gif")) true
            else if (tags.equalsAnyIgnoreCase("image")) false
            else Random.nextDouble() < GIF_CHANCE
        val path = if (isGif) "/cat/gif" else "/cat"
        return "$CAT_API_DOMAIN$path?json=true"
    }

    private fun Iterable<String>.equalsAnyIgnoreCase(string: String): Boolean =
        any { it.equals(string, ignoreCase = true) }

    override suspend fun parseResponse(response: HttpResponse): ApiResponse {
        val body = response.body<ResponseBody>()
        val url = "${CAT_API_DOMAIN}/cat/${body.id}"
        val extension = body.mimetype.split('/', limit = 2)[1]
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
    )
}
