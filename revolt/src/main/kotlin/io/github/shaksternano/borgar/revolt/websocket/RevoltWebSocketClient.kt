package io.github.shaksternano.io.github.shaksternano.borgar.revolt.websocket

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.github.shaksternano.borgar.core.io.httpClient
import io.github.shaksternano.borgar.core.logger
import io.ktor.client.plugins.websocket.*
import io.ktor.util.network.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.concurrent.Executors
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

private val RECONNECT_INTERVAL: Duration = 10.seconds

class RevoltWebSocketClient(
    private val token: String,
) {

    private val messageHandlers: MutableMap<String, MutableList<WebSocketMessageHandler>> = mutableMapOf()
    private var ready: Boolean = false
    private var invalidToken: Boolean = false
    private var open: Boolean = true

    init {
        registerHandlers()
        val threadCount = Runtime.getRuntime().availableProcessors()
        val threadFactory = ThreadFactoryBuilder().setNameFormat("revolt-websocket-%d").build()
        val threadPool = Executors.newFixedThreadPool(threadCount, threadFactory)
        val dispatcher = threadPool.asCoroutineDispatcher()
        CoroutineScope(dispatcher).launch {
            httpClient {
                install(WebSockets)
            }.use { client ->
                runCatching {
                    while (open) {
                        try {
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
                            logger.info("Disconnected from Revolt WebSocket, reconnecting in $RECONNECT_INTERVAL")
                        } catch (e: UnresolvedAddressException) {
                            logger.info("Failed to connect to Revolt WebSocket, trying again in $RECONNECT_INTERVAL")
                        }
                        delay(RECONNECT_INTERVAL)
                    }
                }.onFailure {
                    logger.error("Error with Revolt WebSocket", it)
                }
            }
        }
    }

    private fun registerHandlers() {
        handle(WebSocketMessageType.AUTHENTICATED) {
            logger.info("Logged into Revolt")
        }
        handle(WebSocketMessageType.READY) {
            ready = true
        }
        handle(WebSocketMessageType.NOT_FOUND) {
            invalidToken = true
            open = false
        }
    }

    private fun handle(messageType: WebSocketMessageType, handler: suspend (JsonObject) -> Unit) {
        messageHandlers.getOrPut(messageType.apiName, ::mutableListOf)
            .add(WebSocketMessageHandler(handler))
    }

    suspend fun awaitReady() {
        if (ready) return
        if (invalidToken) throw IllegalArgumentException("Invalid token")
        var resumed = false
        suspendCoroutine { continuation ->
            handle(WebSocketMessageType.READY) {
                if (resumed) return@handle
                resumed = true
                continuation.resume(Unit)
            }
            handle(WebSocketMessageType.NOT_FOUND) {
                if (resumed) return@handle
                resumed = true
                continuation.resumeWithException(IllegalArgumentException("Invalid token"))
            }
        }
    }

    private suspend fun WebSocketSession.handleMessages() {
        if (!open) {
            incoming.cancel()
            return
        }
        coroutineScope {
            for (message in incoming) {
                if (!open) {
                    incoming.cancel()
                    return@coroutineScope
                }
                message as? Frame.Text ?: continue
                val json = Json.parseToJsonElement(message.readText()) as? JsonObject ?: continue
                val type = json["type"] as? JsonPrimitive ?: continue
                val handlers = messageHandlers[type.content] ?: continue
                handlers.forEach {
                    launch {
                        runCatching {
                            it.handleMessage(json)
                        }.onFailure {
                            logger.error("Error handling WebSocket message", it)
                        }
                    }
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
