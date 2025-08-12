package com.shakster.borgar.revolt.websocket

import com.shakster.borgar.core.io.httpClient
import com.shakster.borgar.core.logger
import com.shakster.borgar.core.util.JSON
import com.shakster.borgar.messaging.event.MessageReceiveEvent
import com.shakster.borgar.messaging.exception.InvalidTokenException
import com.shakster.borgar.messaging.util.onMessageReceived
import com.shakster.borgar.revolt.RETRY_CONNECT_INTERVAL
import com.shakster.borgar.revolt.RevoltManager
import com.shakster.borgar.revolt.entity.RevoltGuildResponse
import com.shakster.borgar.revolt.entity.channel.RevoltChannelResponse
import com.shakster.borgar.revolt.entity.channel.RevoltChannelType
import com.shakster.borgar.revolt.entity.createMessage
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.util.network.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.concurrent.Executors
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val PING_JSON: String = "{\"type\":\"Ping\",\"data\":0}"
private val PING_INTERVAL: Duration = 10.seconds

@OptIn(ExperimentalAtomicApi::class)
class RevoltWebSocketClient(
    private val token: String,
    private val manager: RevoltManager,
) {

    private val guildCountAtomic: AtomicInt = AtomicInt(0)
    val guildCount: Int
        get() = guildCountAtomic.load()
    private val messageHandlers: MutableMap<String, MutableList<WebSocketMessageHandler>> = mutableMapOf()
    private var session: DefaultClientWebSocketSession? = null
    private var readyJob: CompletableDeferred<Boolean> = CompletableDeferred()

    suspend fun init() {
        registerHandlers()

        // Create a custom dispatcher to use non-daemon threads
        val threadPool = Executors.newSingleThreadExecutor()
        val dispatcher = threadPool.asCoroutineDispatcher()
        CoroutineScope(dispatcher).launch {
            withContext(Dispatchers.Default) {
                val websocketUrl = Url(manager.webSocketUrl)
                val host = websocketUrl.host
                val path = "${websocketUrl.encodedPath}?version=1&format=json&token=$token"
                while (true) {
                    runCatching {
                        httpClient {
                            install(WebSockets)
                        }.use { client ->
                            client.wss(
                                host = host,
                                path = path,
                            ) {
                                session = this
                                val pingJob = launch {
                                    sendPings()
                                }
                                launch {
                                    awaitReady()
                                }
                                handleMessages()
                                pingJob.cancelAndJoin()
                            }
                            logger.info("Disconnected from Revolt WebSocket, reconnecting...")
                        }
                    }.onFailure {
                        val (logMessage, throwable) = when (it) {
                            // No internet connection
                            is UnresolvedAddressException -> "Failed to connect to Revolt WebSocket." to null

                            is InvalidTokenException -> "Failed to connect to Revolt WebSocket due to invalid token." to null

                            else -> "Error with Revolt WebSocket." to it
                        }
                        logger.error("$logMessage Reconnecting in $RETRY_CONNECT_INTERVAL...", throwable)
                        delay(RETRY_CONNECT_INTERVAL)
                    }
                    session = null
                    readyJob = CompletableDeferred()
                }
            }
        }

        awaitReady()
    }

    private suspend fun awaitReady() {
        val ready = readyJob.await()
        if (!ready) {
            throw InvalidTokenException()
        }
    }

    suspend fun sendTyping(channelId: String) {
        session?.send(
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("BeginTyping"),
                    "channel" to JsonPrimitive(channelId),
                )
            ).toString()
        )
    }

    suspend fun stopTyping(channelId: String) {
        session?.send(
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("EndTyping"),
                    "channel" to JsonPrimitive(channelId),
                )
            ).toString()
        )
    }

    private fun registerHandlers() {
        handle(WebSocketMessageType.AUTHENTICATED) {
            logger.info("Connected to Revolt")
        }
        handle(WebSocketMessageType.READY) {
            readyJob.complete(true)
            val body = JSON.decodeFromJsonElement(ReadyBody.serializer(), it)
            val guildCount = body.guilds.size
            val groupCount = body.channels.count { response ->
                response.type == RevoltChannelType.GROUP.apiName
            }
            guildCountAtomic.store(guildCount + groupCount)
        }
        handle(WebSocketMessageType.NOT_FOUND) {
            readyJob.complete(false)
        }
        handle(WebSocketMessageType.MESSAGE) {
            handleMessage(it)
        }
        handle(WebSocketMessageType.SERVER_CREATE) {
            guildCountAtomic.incrementAndFetch()
        }
        handle(WebSocketMessageType.SERVER_DELETE) {
            guildCountAtomic.decrementAndFetch()
        }
        handle(WebSocketMessageType.SERVER_MEMBER_LEAVE) {
            val userId = it["user"] as? JsonPrimitive
            if (userId?.content == manager.selfId) {
                guildCountAtomic.decrementAndFetch()
            }
        }
        handle(WebSocketMessageType.CHANNEL_CREATE) {
            val channelType = it["channel_type"] as? JsonPrimitive
            if (channelType?.content == "Group") {
                guildCountAtomic.incrementAndFetch()
            }
        }
        handle(WebSocketMessageType.CHANNEL_GROUP_LEAVE) {
            val userId = it["user"] as? JsonPrimitive
            if (userId?.content == manager.selfId) {
                guildCountAtomic.decrementAndFetch()
            }
        }
    }

    private suspend fun handleMessage(json: JsonObject) {
        val message = createMessage(json, manager)
        val event = MessageReceiveEvent(message)
        onMessageReceived(event)
    }

    private fun handle(messageType: WebSocketMessageType, handler: suspend (JsonObject) -> Unit) {
        messageHandlers.getOrPut(messageType.apiName, ::mutableListOf)
            .add(WebSocketMessageHandler(handler))
    }

    private suspend fun WebSocketSession.sendPings() {
        while (true) {
            send(PING_JSON)
            delay(PING_INTERVAL)
        }
    }

    private suspend fun WebSocketSession.handleMessages() {
        coroutineScope {
            for (message in incoming) {
                message as? Frame.Text ?: continue
                val json = Json.parseToJsonElement(message.readText()) as? JsonObject ?: continue
                val type = json["type"] as? JsonPrimitive ?: continue
                val handlers = messageHandlers[type.content] ?: continue
                handlers.forEach { handler ->
                    launch {
                        runCatching {
                            handler.handleMessage(json)
                        }.onFailure { throwable ->
                            logger.error("Error handling Revolt WebSocket message", throwable)
                        }
                    }
                }
            }
        }
    }

    @Serializable
    private data class ReadyBody(
        @SerialName("servers")
        val guilds: List<RevoltGuildResponse>,
        val channels: List<RevoltChannelResponse>,
    )
}
