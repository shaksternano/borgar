package io.github.shaksternano.borgar.revolt.entity

import io.github.shaksternano.borgar.core.io.head
import io.github.shaksternano.borgar.core.io.useHttpClient
import io.github.shaksternano.borgar.core.util.encodeUrl
import io.github.shaksternano.borgar.messaging.entity.BaseEntity
import io.github.shaksternano.borgar.messaging.entity.CustomEmoji
import io.github.shaksternano.borgar.revolt.RevoltManager
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class RevoltCustomEmoji(
    override val manager: RevoltManager,
    override val id: String,
    override val name: String,
    override val imageUrl: String,
) : CustomEmoji, BaseEntity() {

    override val asMention: String = manager.emojiAsTyped(id)
    override val asBasicMention: String = manager.emojiAsTyped(id)
}

@Serializable
data class RevoltEmojiResponse(
    @SerialName("_id")
    val id: String,
    val name: String,
    val animated: Boolean = false,
) {
    suspend fun convert(manager: RevoltManager): RevoltCustomEmoji {
        val url = "${manager.cdnUrl}/emojis/$id"
        val extension = runCatching {
            useHttpClient { client ->
                client.head(url)
                    .contentType()
                    ?.contentSubtype
                    ?.lowercase()
            }
        }.getOrNull().let {
            if (it.isNullOrBlank()) {
                if (animated) "gif" else "png"
            } else {
                it
            }
        }
        return RevoltCustomEmoji(
            manager = manager,
            id = id,
            name = name,
            imageUrl = "$url/${name.encodeUrl()}.$extension"
        )
    }
}
