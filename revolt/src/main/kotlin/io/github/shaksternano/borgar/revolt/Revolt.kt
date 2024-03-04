package io.github.shaksternano.borgar.revolt

import io.github.shaksternano.io.github.shaksternano.borgar.revolt.RevoltManager

suspend fun initRevolt(token: String) {
    RevoltManager(token).awaitReady()
}
