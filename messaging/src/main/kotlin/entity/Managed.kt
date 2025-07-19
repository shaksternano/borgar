package io.github.shaksternano.borgar.messaging.entity

import io.github.shaksternano.borgar.messaging.BotManager

interface Managed {

    val manager: BotManager
}