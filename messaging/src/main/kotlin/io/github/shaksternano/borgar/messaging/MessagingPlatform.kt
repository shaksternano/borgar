package io.github.shaksternano.borgar.messaging

import io.github.shaksternano.borgar.core.util.Displayed

enum class MessagingPlatform(
    val id: String,
    override val displayName: String
) : Displayed {
    DISCORD("discord", "Discord"),
    REVOLT("revolt", "Revolt"),
}
