package io.github.shaksternano.borgar.revolt

import io.github.shaksternano.borgar.messaging.registerBotManager

suspend fun initRevolt(token: String) {
    val manager = RevoltManager(token)
    manager.init()
    registerBotManager(manager)
}
