package io.github.shaksternano.io.github.shaksternano.borgar.revolt.websocket

import io.github.shaksternano.borgar.core.io.httpClient
import io.github.shaksternano.borgar.core.logger
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val REVOLT_WEBSOCKET_URL = "ws.revolt.chat"

private val PING_JSON: String =
    JsonObject(mapOf(
        "type" to JsonPrimitive("Ping"),
        "data" to JsonPrimitive(0),
    )).toString()
private val PING_INTERVAL: Duration = 10.seconds

class RevoltWebSocketClient(
    private val token: String,
) {

    private val messageHandlers: MutableMap<String, MutableList<WebSocketMessageHandler>> = mutableMapOf()
    private var ready: Boolean = false
    private var invalidToken: Boolean = false
    private var open: Boolean = true

    init {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            runCatching {
                httpClient {
                    install(WebSockets)
                }.use { client ->
                    client.webSocket(
                        host = REVOLT_WEBSOCKET_URL,
                        path = "?version=1&format=json&token=$token",
                    ) {
                        val pingJob = launch {
                            sendPings()
                        }
                        handleMessages()
                        pingJob.cancel()
                    }
                }
            }.onFailure {
                logger.error("Error with Revolt WebSocket", it)
            }
        }
        handle(WebSocketMessageType.AUTHENTICATED) {
            logger.info("Logged into Revolt")
        }
    }

    private fun handle(messageType: WebSocketMessageType, handler: suspend (JsonObject) -> Unit) {
        messageHandlers.getOrPut(messageType.apiName, ::mutableListOf)
            .add(WebSocketMessageHandler(handler))
    }

    suspend fun awaitReady() {
        if (ready) return
        if (invalidToken) throw IllegalArgumentException("Invalid token")
        suspendCoroutine { continuation ->
            handle(WebSocketMessageType.READY) {
                ready = true
                continuation.resume(Unit)
            }
            handle(WebSocketMessageType.NOT_FOUND) {
                invalidToken = true
                open = false
                continuation.resumeWithException(IllegalArgumentException("Invalid token"))
            }
        }
    }

    private suspend fun WebSocketSession.handleMessages() {
        if (!open) {
            incoming.cancel()
            return
        }
        for (message in incoming) {
            if (!open) {
                incoming.cancel()
                return
            }
            message as? Frame.Text ?: continue
            val json = Json.parseToJsonElement(message.readText()) as? JsonObject ?: continue
            val type = json["type"] as? JsonPrimitive ?: continue
            val handlers = messageHandlers[type.content] ?: continue
            handlers.forEach {
                runCatching {
                    it.handleMessage(json)
                }.onFailure {
                    logger.error("Error handling WebSocket message", it)
                }
            }
        }
    }

    private suspend fun WebSocketSession.sendPings() {
        while (open) {
            send(PING_JSON)
            delay(PING_INTERVAL)
        }
    }
}
