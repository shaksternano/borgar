package io.github.shaksternano.borgar.revolt

import io.github.shaksternano.borgar.chat.registerBotManager

suspend fun initRevolt(token: String) {
    val manager = RevoltManager(token)
    registerBotManager(manager)
    manager.awaitReady()
}
