package io.github.shaksternano.borgar.messaging.entity

import io.github.shaksternano.borgar.core.io.UrlInfo
import io.github.shaksternano.borgar.core.util.TenorMediaType
import io.github.shaksternano.borgar.core.util.isTenorUrl
import io.github.shaksternano.borgar.core.util.retrieveTenorMediaUrl

data class MessageEmbed(
    val url: String?,
    val image: ImageInfo? = null,
    val video: VideoInfo? = null,
    val thumbnail: ThumbnailInfo? = null,
) {

    data class ImageInfo(
        val url: String?,
        val proxyUrl: String?,
    )

    data class VideoInfo(
        val url: String?,
        val proxyUrl: String?,
    )

    data class ThumbnailInfo(
        val url: String?,
        val proxyUrl: String?,
    )
}

suspend fun MessageEmbed.getContent(getGif: Boolean = false): UrlInfo? {
    val isTenor = url?.let(::isTenorUrl) ?: false
    val tenorGifUrl = if (getGif && url != null)
        retrieveTenorMediaUrl(url, TenorMediaType.GIF_LARGE)
    else null
    val url = tenorGifUrl
        ?: video?.proxyUrl
        ?: video?.url
        ?: image?.proxyUrl
        ?: image?.url
        ?: thumbnail?.proxyUrl
        ?: thumbnail?.url
    return url?.let {
        UrlInfo(
            url = it,
            gifv = isTenor && tenorGifUrl == null
        )
    }
}
