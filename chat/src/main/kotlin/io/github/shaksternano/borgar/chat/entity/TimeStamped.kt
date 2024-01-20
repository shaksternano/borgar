package io.github.shaksternano.borgar.chat.entity

import java.time.OffsetDateTime

interface TimeStamped {
    val timeCreated: OffsetDateTime
}
