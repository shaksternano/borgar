package com.shakster.borgar.revolt.entity

import com.shakster.borgar.messaging.entity.BaseEntity
import com.shakster.borgar.messaging.entity.CustomEmoji
import com.shakster.borgar.revolt.RevoltManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class RevoltCustomEmoji(
    override val id: String,
    override val name: String,
    override val imageUrl: String,
    override val manager: RevoltManager,
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
    fun convert(manager: RevoltManager): RevoltCustomEmoji {
        return RevoltCustomEmoji(
            id = id,
            name = name,
            imageUrl = "${manager.cdnUrl}/emojis/$id/original",
            manager = manager,
        )
    }
}
