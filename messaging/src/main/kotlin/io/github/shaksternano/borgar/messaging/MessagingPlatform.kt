package io.github.shaksternano.borgar.messaging

import io.github.shaksternano.borgar.core.util.Displayed
import io.github.shaksternano.borgar.core.util.Identified

enum class MessagingPlatform(
    override val id: String,
    override val displayName: String
) : Identified, Displayed {
    DISCORD("discord", "Discord"),
    REVOLT("revolt", "Revolt"),
}
