package com.shakster.borgar.revolt

import com.shakster.borgar.messaging.registerBotManager

suspend fun initRevolt(token: String) {
    val manager = RevoltManager(token)
    manager.init()
    registerBotManager(manager)
}
