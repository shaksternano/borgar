package com.shakster.borgar.stoat

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
import com.shakster.borgar.stoat.entity.*
import com.shakster.borgar.stoat.entity.channel.StoatChannel
import com.shakster.borgar.stoat.entity.channel.StoatChannelResponse
import com.shakster.borgar.stoat.util.toStoat
import com.shakster.borgar.stoat.websocket.StoatWebSocketClient
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

private const val STOAT_API_URL: String = "https://api.stoat.chat"
private const val STOAT_TOKEN_HEADER: String = "x-bot-token"
private val USER_MENTION_REGEX: Regex = "<@[A-Za-z0-9]+>".toRegex()
val USER_SILENT_MENTION_REGEX: Regex = """<\\@[a-zA-Z0-9]+>""".toRegex()
val RETRY_CONNECT_INTERVAL: Duration = 10.seconds

class StoatManager(
    private val token: String,
) : BotManager {

    val apiUrl: String = BotConfig.get().stoat.apiUrl.ifBlank {
        STOAT_API_URL
    }
    lateinit var webSocketUrl: String
        private set
    lateinit var cdnUrl: String
        private set
    lateinit var proxyUrl: String
        private set
    lateinit var appUrl: String
        private set

    val webSocket: StoatWebSocketClient = StoatWebSocketClient(token, this)

    override val platform: MessagingPlatform = MessagingPlatform.STOAT
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

    private val systemUser: StoatUser = StoatUser(
        manager = this,
        id = "00000000000000000000000000",
        name = "System",
        effectiveName = "System",
        effectiveAvatarUrl = "https://cdn.stoatusercontent.com/attachments/dApW6WWX6KysecIPe14Ifz9t2OawkmwGfdilfdWMjS/stoat.png",
        isBot = false,
    )

    private var ready: Boolean = false

    suspend fun init() {
        if (ready) return

        val apiBody = useHttpClient { client ->
            client.get(apiUrl).body<StoatApiBody>()
        }

        webSocketUrl = apiBody.ws
        cdnUrl = apiBody.features.autumn.url
        proxyUrl = apiBody.features.january.url
        appUrl = apiBody.app

        val self = updateStatusAndGetSelf()
        selfId = self.id
        ownerId = self.ownerId ?: error("Stoat bot owner ID not found")

        webSocket.init()
        ready = true
    }

    private suspend fun updateStatusAndGetSelf(): StoatUser {
        val editUserBody = EditUserRequest(
            status = StatusBody(BOT_STATUS),
        )
        // Sometimes fails randomly, so we retry until it succeeds
        while (true) {
            try {
                val self = request<StoatUserResponse>(
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

    override suspend fun getSelf(): StoatUser =
        runCatching {
            request<StoatUserResponse>("/users/@me")
        }.getOrElse {
            throw IllegalStateException("Failed to get Stoat self user", it)
        }.convert(this)

    override suspend fun getChannel(id: String): StoatChannel? =
        runCatching {
            request<StoatChannelResponse>("/channels/$id")
        }.getOrNull()?.convert(this)

    override suspend fun getGuild(id: String): StoatGuild? =
        runCatching {
            request<StoatGuildResponse>("/servers/$id")
        }.getOrNull()?.convert(this)

    override suspend fun getGroup(id: String): StoatGroup? =
        getChannel(id)?.getGroup()

    override suspend fun getUser(id: String): StoatUser? =
        if (id == systemUser.id) systemUser
        else runCatching {
            request<StoatUserResponse>("/users/$id")
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
                    val response = request<StoatEmojiResponse>("/custom/emoji/$it")
                    response.convert(this@StoatManager)
                }.getOrNull()
            }

    override fun getMentionedUsers(content: String): Flow<User> {
        val mentionedUserIds = USER_MENTION_REGEX.findAll(content)
            .map {
                it.value.removeSurrounding("<@", ">")
            } + USER_SILENT_MENTION_REGEX.findAll(content)
            .map {
                it.value.removeSurrounding("<\\@", ">")
            }
        return mentionedUserIds.asFlow()
            .mapNotNull {
                getUser(it)
            }
    }

    override fun getMentionedChannels(content: String): Flow<Channel> = emptyFlow()

    override fun getMentionedRoles(content: String): Flow<Role> = emptyFlow()

    override fun getEmojiName(typedEmoji: String): String =
        typedEmoji.removeSurrounding(":")

    override fun emojiAsTyped(emoji: String): String =
        ":$emoji:"

    override fun getPermissionName(permission: Permission): String =
        permission.toStoat().displayName

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
                    append(STOAT_TOKEN_HEADER, token)
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
                    append(STOAT_TOKEN_HEADER, token)
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

    @Serializable
    private data class StoatApiBody(
        val features: StoatFeaturesBody,
        val ws: String,
        val app: String,
    )

    @Serializable
    private data class StoatFeaturesBody(
        val autumn: StoatFeatureBody,
        val january: StoatFeatureBody,
    )

    @Serializable
    private data class StoatFeatureBody(
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
