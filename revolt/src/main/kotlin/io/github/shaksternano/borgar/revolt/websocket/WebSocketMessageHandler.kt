package io.github.shaksternano.borgar.revolt.websocket

import kotlinx.serialization.json.JsonObject

fun interface WebSocketMessageHandler {

    suspend fun handleMessage(json: JsonObject)
}
