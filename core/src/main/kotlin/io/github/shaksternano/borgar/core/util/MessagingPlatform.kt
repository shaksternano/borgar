package io.github.shaksternano.borgar.core.util

enum class MessagingPlatform(
    override val id: String,
    override val displayName: String
) : Identified, Displayed {
    DISCORD("discord", "Discord"),
    REVOLT("revolt", "Revolt"),
}
