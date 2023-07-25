package io.github.shaksternano.borgar.command

import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable

private const val PONY_API_DOMAIN = "https://www.theponyapi.com"

class PonyCommand(
    name: String,
    description: String,
    count: Int? = null,
) : ApiFilesCommand(
    name,
    description,
    count,
    "pony",
) {

    override fun requestUrl(): String = "$PONY_API_DOMAIN/api/v1/pony/random"

    override suspend fun parseResponse(response: HttpResponse): ApiResponse {
        val body = response.body<ResponseBody>()
        return ApiResponse(
            body.pony.id.toString(),
            body.pony.representations.full,
            body.pony.originalFormat,
        )
    }

    @Serializable
    private data class ResponseBody(
        val pony: PonyBody,
    )

    @Serializable
    private data class PonyBody(
        val id: Long,
        val originalFormat: String,
        val representations: Representations,
    )

    @Serializable
    private data class Representations(
        val full: String,
    )
}
