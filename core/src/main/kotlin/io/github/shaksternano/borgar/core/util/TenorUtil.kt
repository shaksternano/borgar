package io.github.shaksternano.borgar.core.util

import io.github.shaksternano.borgar.core.io.UrlInfo
import io.github.shaksternano.borgar.core.io.httpGet
import kotlinx.serialization.Serializable
import java.net.URI

private const val DEFAULT_TENOR_API_KEY: String = "LIVDSRZULELA"

suspend fun retrieveTenorMediaUrl(url: String, getGif: Boolean): UrlInfo? {
    val mediaType =
        if (getGif) TenorMediaType.GIF_LARGE
        else TenorMediaType.MP4_NORMAL
    val mediaUrl = retrieveTenorMediaUrl(url, mediaType) ?: return null
    return UrlInfo(
        url = mediaUrl,
        gifv = !getGif,
    )
}

suspend fun retrieveTenorMediaUrl(url: String, mediaType: TenorMediaType): String? {
    if (!isTenorUrl(url)) return null
    val apiKey = getEnvVar("TENOR_API_KEY") ?: DEFAULT_TENOR_API_KEY
    val mediaId = url.substring(url.lastIndexOf("-") + 1)
    val requestUrl = "https://g.tenor.com/v1/gifs?key=$apiKey&ids=$mediaId"
    val responseBody = httpGet<TenorResponse>(requestUrl)
    return runCatching {
        responseBody.results.first().media.first().getOrElse(mediaType.id) {
            throw IllegalArgumentException("Media type $mediaType not found")
        }.url
    }.getOrElse {
        throw IllegalStateException("Error parsing Tenor response", it)
    }
}

fun isTenorUrl(url: String): Boolean {
    val uri = runCatching { URI(url) }.getOrElse { return false }
    val host = uri.host
    return host != null && host.contains("tenor.com") && uri.getPath().startsWith("/view/")
}

/**
 * Contains Tenor string constants for different media types.
 */
@Suppress("unused")
enum class TenorMediaType(
    override val id: String,
    override val displayName: String
) : Identified, Displayed {
    GIF_EXTRA_SMALL("nanogif", "Extra small GIF"),
    GIF_SMALL("tinygif", "Small GIF"),
    GIF_NORMAL("gif", "Normal GIF"),
    GIF_LARGE("mediumgif", "Large GIF"),

    MP4_EXTRA_SMALL("nanomp4", "Extra small MP4"),
    MP4_SMALL("tinymp4", "Small MP4"),
    MP4_NORMAL("mp4", "Normal MP4"),
    MP4_NORMAL_LOOPED("loopedmp4", "Normal looped MP4"),

    WEBM_EXTRA_SMALL("nanowebm", "Extra small WebM"),
    WEBM_SMALL("tinywebm", "Small WebM"),
    WEBM_NORMAL("webm", "Normal WebM"),
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
