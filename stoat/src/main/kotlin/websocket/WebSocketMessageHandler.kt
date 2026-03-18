package com.shakster.borgar.stoat.websocket

import kotlinx.serialization.json.JsonObject

fun interface WebSocketMessageHandler {

    suspend fun handleMessage(json: JsonObject)
}
