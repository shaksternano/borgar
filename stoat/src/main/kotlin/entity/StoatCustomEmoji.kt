package com.shakster.borgar.stoat.entity

import com.shakster.borgar.messaging.entity.BaseEntity
import com.shakster.borgar.messaging.entity.CustomEmoji
import com.shakster.borgar.stoat.StoatManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class StoatCustomEmoji(
    override val id: String,
    override val name: String,
    override val imageUrl: String,
    override val manager: StoatManager,
) : CustomEmoji, BaseEntity() {

    override val asMention: String = manager.emojiAsTyped(id)
    override val asBasicMention: String = manager.emojiAsTyped(id)
}

@Serializable
data class StoatEmojiResponse(
    @SerialName("_id")
    val id: String,
    val name: String,
    val animated: Boolean = false,
) {
    fun convert(manager: StoatManager): StoatCustomEmoji {
        return StoatCustomEmoji(
            id = id,
            name = name,
            imageUrl = "${manager.cdnUrl}/emojis/$id/original",
            manager = manager,
        )
    }
}
