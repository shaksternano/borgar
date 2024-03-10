package io.github.shaksternano.borgar.messaging.event

import io.github.shaksternano.borgar.messaging.entity.User

interface UserInteractionEvent : InteractionEvent {

    val user: User
}
