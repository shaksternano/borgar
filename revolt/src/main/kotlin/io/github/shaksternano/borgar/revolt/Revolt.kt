package io.github.shaksternano.borgar.revolt

suspend fun initRevolt(token: String) {
    RevoltManager(token).awaitReady()
}
