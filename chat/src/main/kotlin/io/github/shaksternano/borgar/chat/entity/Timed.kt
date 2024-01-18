package io.github.shaksternano.borgar.chat.entity

import java.time.OffsetDateTime

interface Timed {
    val timeCreated: OffsetDateTime
}
