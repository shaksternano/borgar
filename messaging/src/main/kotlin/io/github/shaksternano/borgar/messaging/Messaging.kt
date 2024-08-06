package io.github.shaksternano.borgar.messaging

import io.github.shaksternano.borgar.messaging.command.DerpibooruCommand

suspend fun initMessaging() {
    DerpibooruCommand.loadTags()
}
