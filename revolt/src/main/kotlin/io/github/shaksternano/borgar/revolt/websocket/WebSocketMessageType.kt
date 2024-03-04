package io.github.shaksternano.io.github.shaksternano.borgar.revolt.websocket

enum class WebSocketMessageType(
    val apiName: String,
) {
    READY("Ready"),
    AUTHENTICATED("Authenticated"),
    NOT_FOUND("NotFound"),
}
