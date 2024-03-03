package io.github.shaksternano.borgar.chat.event

import io.github.shaksternano.borgar.chat.entity.User

interface UserInteractionEvent : InteractionEvent {

    val user: User
}
