package com.shakster.borgar.revolt

import com.shakster.borgar.core.BotConfig
import com.shakster.borgar.core.exception.HttpException
import com.shakster.borgar.core.io.request
import com.shakster.borgar.core.io.useHttpClient
import com.shakster.borgar.core.util.MessagingPlatform
import com.shakster.borgar.core.util.prettyPrintJsonCatching
import com.shakster.borgar.messaging.BOT_STATUS
import com.shakster.borgar.messaging.BotManager
import com.shakster.borgar.messaging.command.Permission
import com.shakster.borgar.messaging.entity.CustomEmoji
import com.shakster.borgar.messaging.entity.Role
import com.shakster.borgar.messaging.entity.User
import com.shakster.borgar.messaging.entity.channel.Channel
import com.shakster.borgar.messaging.exception.InvalidTokenException
import com.shakster.borgar.revolt.entity.*
import com.shakster.borgar.revolt.entity.channel.RevoltChannel
import com.shakster.borgar.revolt.entity.channel.RevoltChannelResponse
import com.shakster.borgar.revolt.util.toRevolt
import com.shakster.borgar.revolt.websocket.RevoltWebSocketClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val REVOLT_TOKEN_HEADER: String = "x-bot-token"
private val USER_MENTION_REGEX: Regex = "<@[A-Za-z0-9]+>".toRegex()
val RETRY_CONNECT_INTERVAL: Duration = 10.seconds

class RevoltManager(
    private val token: String,
) : BotManager {

    val apiUrl: String = BotConfig.get().revolt.apiUrl.ifBlank {
        "https://api.revolt.chat/0.8"
    }
    lateinit var webSocketUrl: String
        private set
    lateinit var cdnUrl: String
        private set
    lateinit var proxyUrl: String
        private set
    lateinit var appUrl: String
        private set

    val webSocket: RevoltWebSocketClient = RevoltWebSocketClient(token, this)

    override val platform: MessagingPlatform = MessagingPlatform.REVOLT
    override var selfId: String = ""
        private set
    override var ownerId: String = ""
        private set
    override val maxMessageContentLength: Int = 2000
    override val maxFileSize: Long = 20 * 1024 * 1024
    override val maxFilesPerMessage: Int = 5
    override val emojiTypedRegex: Regex = ":[A-Za-z0-9]+:".toRegex()
    override val typingDuration: Duration = 1.seconds
    override val commandAutoCompleteMaxSuggestions: Int = 0

    private val systemUser: RevoltUser = RevoltUser(
        manager = this,
        id = "00000000000000000000000000",
        name = "System",
        effectiveName = "System",
        effectiveAvatarUrl = "https://cdn.revoltusercontent.com/attachments/7HzJPSqop6nEMrnlH3tpqiWe31gX8pmeQxiUxkGxPn/revolt.png",
        isBot = false,
    )

    private var ready: Boolean = false

    suspend fun init() {
        if (ready) return

        val apiBody = useHttpClient { client ->
            client.get(apiUrl).body<RevoltApiBody>()
        }

        webSocketUrl = apiBody.ws
        cdnUrl = apiBody.features.autumn.url
        proxyUrl = apiBody.features.january.url
        appUrl = apiBody.app

        val self = updateStatusAndGetSelf()
        selfId = self.id
        ownerId = self.ownerId ?: error("Revolt bot owner ID not found")

        webSocket.init()
        ready = true
    }

    private suspend fun updateStatusAndGetSelf(): RevoltUser {
        val editUserBody = EditUserRequest(
            status = StatusBody(BOT_STATUS),
        )
        // Sometimes fails randomly, so we retry until it succeeds
        while (true) {
            try {
                val self = request<RevoltUserResponse>(
                    path = "/users/@me",
                    method = HttpMethod.Patch,
                    body = editUserBody,
                ).convert(this)
                return self
            } catch (e: InvalidTokenException) {
                throw e
            } catch (_: Throwable) {
                delay(RETRY_CONNECT_INTERVAL)
            }
        }
    }

    override suspend fun getSelf(): RevoltUser =
        runCatching {
            request<RevoltUserResponse>("/users/@me")
        }.getOrElse {
            throw IllegalStateException("Failed to get Revolt self user", it)
        }.convert(this)

    override suspend fun getChannel(id: String): RevoltChannel? =
        runCatching {
            request<RevoltChannelResponse>("/channels/$id")
        }.getOrNull()?.convert(this)

    override suspend fun getGuild(id: String): RevoltGuild? =
        runCatching {
            request<RevoltGuildResponse>("/servers/$id")
        }.getOrNull()?.convert(this)

    override suspend fun getGroup(id: String): RevoltGroup? =
        getChannel(id)?.getGroup()

    override suspend fun getUser(id: String): RevoltUser? =
        if (id == systemUser.id) systemUser
        else runCatching {
            request<RevoltUserResponse>("/users/$id")
        }.getOrNull()?.convert(this)

    override suspend fun getGuildCount(): Int = webSocket.guildCount

    override fun getCustomEmojis(content: String): Flow<CustomEmoji> =
        emojiTypedRegex.findAll(content)
            .map {
                getEmojiName(it.value)
            }
            .asFlow()
            .mapNotNull {
                runCatching {
                    val response = request<RevoltEmojiResponse>("/custom/emoji/$it")
                    response.convert(this@RevoltManager)
                }.getOrNull()
            }

    override fun getMentionedUsers(content: String): Flow<User> =
        USER_MENTION_REGEX.findAll(content)
            .map {
                getUserId(it.value)
            }
            .asFlow()
            .mapNotNull {
                getUser(it)
            }

    override fun getMentionedChannels(content: String): Flow<Channel> = emptyFlow()

    override fun getMentionedRoles(content: String): Flow<Role> = emptyFlow()

    override fun getEmojiName(typedEmoji: String): String =
        typedEmoji.removeSurrounding(":")

    override fun emojiAsTyped(emoji: String): String =
        ":$emoji:"

    override fun getPermissionName(permission: Permission): String =
        permission.toRevolt().displayName

    override fun formatUserMention(userId: String): String =
        "<@$userId>"

    suspend inline fun <reified T> request(
        path: String,
        method: HttpMethod = HttpMethod.Get,
        headers: Map<String, String> = emptyMap(),
        body: Any? = null,
    ): T =
        useHttpClient { client ->
            val response = request(client, path, method, headers, body)
            response.body<T>()
        }

    suspend fun request(
        client: HttpClient,
        path: String,
        method: HttpMethod = HttpMethod.Get,
        headers: Map<String, String> = emptyMap(),
        body: Any? = null,
        ignoreErrors: Boolean = false,
    ): HttpResponse {
        val url = "$apiUrl$path"
        return runCatching {
            client.request(url) {
                this.method = method
                headers {
                    append(REVOLT_TOKEN_HEADER, token)
                    headers.forEach { (key, value) ->
                        append(key, value)
                    }
                }
                if (body != null) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            }
        }.getOrElse {
            throw IOException("Failed to ${method.value} request $url", it)
        }.also {
            val status = it.status
            if (status == HttpStatusCode.Unauthorized) {
                throw InvalidTokenException()
            }
            if (!ignoreErrors && !status.isSuccess()) {
                val errorBody = runCatching {
                    it.bodyAsText()
                }.getOrElse { "" }
                var error = "${method.value} request to $url returned error response: ${it.status}"
                if (errorBody.isNotBlank()) {
                    val jsonError = prettyPrintJsonCatching(errorBody)
                    error += ". Body:\n$jsonError"
                }
                throw IOException(error)
            }
        }
    }

    suspend inline fun <reified T> postCdnForm(path: String, form: List<PartData>) =
        useHttpClient { client ->
            val response = postCdnForm(client, path, form)
            response.body<T>()
        }

    suspend fun postCdnForm(
        client: HttpClient,
        path: String,
        form: List<PartData>,
    ): HttpResponse {
        val url = "$cdnUrl$path"
        return runCatching {
            client.submitFormWithBinaryData(url, form) {
                headers {
                    append(REVOLT_TOKEN_HEADER, token)
                }
            }
        }.getOrElse {
            throw IOException("Failed to post form data to $url", it)
        }.also {
            if (!it.status.isSuccess()) {
                throw HttpException("Posting form data to $url returned error response: ${it.status}", it.status)
            }
        }
    }

    private fun getUserId(typedUserMention: String): String =
        typedUserMention.removeSurrounding("<@", ">")

    @Serializable
    private data class RevoltApiBody(
        val features: RevoltFeaturesBody,
        val ws: String,
        val app: String,
    )

    @Serializable
    private data class RevoltFeaturesBody(
        val autumn: RevoltFeatureBody,
        val january: RevoltFeatureBody,
    )

    @Serializable
    private data class RevoltFeatureBody(
        val url: String,
    )

    @Serializable
    private data class EditUserRequest(
        val status: StatusBody,
    )

    @Serializable
    private data class StatusBody(
        val text: String,
    )
}
