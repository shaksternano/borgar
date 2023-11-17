package io.github.shaksternano.borgar.core.util

import io.github.shaksternano.borgar.core.io.httpGet
import kotlinx.serialization.Serializable
import java.net.URI
import kotlin.jvm.optionals.getOrDefault

private const val DEFAULT_TENOR_API_KEY: String = "LIVDSRZULELA"

suspend fun retrieveTenorMediaUrl(url: String, mediaType: TenorMediaType): String? {
    val uri = runCatching { URI(url) }.getOrElse { return null }
    val host = uri.host
    if (host == null || !host.contains("tenor.com") || !uri.getPath().startsWith("/view/"))
        return null
    val apiKey = Environment.getEnvVar("TENOR_API_KEY").getOrDefault(DEFAULT_TENOR_API_KEY)
    val mediaId = url.substring(url.lastIndexOf("-") + 1)
    val requestUrl = "https://g.tenor.com/v1/gifs?key=$apiKey&ids=$mediaId"
    val responseBody = httpGet<TenorResponse>(requestUrl)
    return runCatching {
        responseBody.results.first().media.first().getOrElse(mediaType.key) {
            throw IllegalArgumentException("Media type $mediaType not found")
        }.url
    }.getOrElse {
        throw IllegalStateException("Error parsing Tenor response", it)
    }
}

/**
 * Contains Tenor string constants for different media types.
 */
@Suppress("unused")
enum class TenorMediaType(val key: String) {
    GIF_EXTRA_SMALL("nanogif"),
    GIF_SMALL("tinygif"),
    GIF_NORMAL("gif"),
    GIF_LARGE("mediumgif"),

    MP4_EXTRA_SMALL("nanomp4"),
    MP4_SMALL("tinymp4"),
    MP4_NORMAL("mp4"),
    MP4_NORMAL_LOOPED("loopedmp4"),

    WEBM_EXTRA_SMALL("nanowebm"),
    WEBM_SMALL("tinywebm"),
    WEBM_NORMAL("webm"),
}

@Serializable
private data class TenorResponse(
    val results: List<Result>,
)

@Serializable
private data class Result(
    val media: List<Map<String, Media>>,
)

@Serializable
private data class Media(
    val url: String,
)
