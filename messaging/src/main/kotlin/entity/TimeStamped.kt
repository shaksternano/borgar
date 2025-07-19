package io.github.shaksternano.borgar.messaging.entity

import java.time.OffsetDateTime

interface TimeStamped {
    val timeCreated: OffsetDateTime
}
