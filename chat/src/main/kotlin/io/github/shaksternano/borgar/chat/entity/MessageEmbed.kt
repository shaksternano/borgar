package io.github.shaksternano.borgar.chat.entity

data class MessageEmbed(
    val image: ImageInfo?,
    val video: VideoInfo?,
) {
    data class ImageInfo(
        val url: String?,
    )

    data class VideoInfo(
        val url: String?,
    )
}
