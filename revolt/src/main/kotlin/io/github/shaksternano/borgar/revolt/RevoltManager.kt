package io.github.shaksternano.borgar.revolt

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.command.Permission
import io.github.shaksternano.borgar.chat.entity.CustomEmoji
import io.github.shaksternano.borgar.chat.entity.Role
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.core.io.useHttpClient
import io.github.shaksternano.borgar.revolt.entity.RevoltGuild
import io.github.shaksternano.borgar.revolt.entity.RevoltGuildResponse
import io.github.shaksternano.borgar.revolt.entity.RevoltUser
import io.github.shaksternano.borgar.revolt.entity.RevoltUserResponse
import io.github.shaksternano.borgar.revolt.websocket.RevoltWebSocketClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.errors.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

const val REVOLT_API_DOMAIN = "https://api.revolt.chat"
const val REVOLT_CDN_DOMAIN = "https://autumn.revolt.chat"
const val REVOLT_TOKEN_HEADER = "x-bot-token"

class RevoltManager(
    private val token: String,
) : BotManager {

    override var selfId: String = ""
        private set
    override var ownerId: String = ""
        private set
    override val maxMessageContentLength: Int = 2000
    override val maxFileSize: Long = 20 * 1024 * 1024
    override val maxFilesPerMessage: Int = 5
    override val emojiTypedPattern: Regex = ":[A-Za-z0-9]+:".toRegex()
    override val typingDuration: Duration = 1.seconds
    val webSocket: RevoltWebSocketClient = RevoltWebSocketClient(token, this)
    val apiDomain: String = REVOLT_API_DOMAIN
    val cdnDomain: String = REVOLT_CDN_DOMAIN
    private var ready: Boolean = false

    suspend fun awaitReady() {
        if (ready) return
        webSocket.awaitReady()
        val self = getSelf()
        selfId = self.id
        ownerId = self.ownerId ?: error("Owner ID not found")
        ready = true
    }

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

    suspend fun request(path: String, method: HttpMethod = HttpMethod.Get) {
        useHttpClient {
            request(it, path, method)
        }
    }

    suspend fun request(
        client: HttpClient,
        path: String,
        method: HttpMethod = HttpMethod.Get,
        headers: Map<String, String> = emptyMap(),
        body: Any? = null,
    ): HttpResponse {
        val url = "$apiDomain$path"
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
            throw IOException("Failed to ${method.value} $url", it)
        }.also {
            require(it.status.isSuccess()) {
                "${method.value} request to $url returned error response: ${it.status}"
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
        form: List<PartData>
    ): HttpResponse {
        val url = "$cdnDomain$path"
        return runCatching {
            client.submitFormWithBinaryData(url, form) {
                headers {
                    append(REVOLT_TOKEN_HEADER, token)
                }
            }
        }.getOrElse {
            throw IOException("Failed to post form data to $url", it)
        }.also {
            require(it.status.isSuccess()) {
                "Posting form data to $url returned error response: ${it.status}"
            }
        }
    }

    override suspend fun getSelf(): RevoltUser =
        runCatching {
            request<RevoltUserResponse>("/users/@me")
        }.getOrElse {
            throw IllegalStateException("Failed to get self user", it)
        }.convert(this)

    override suspend fun getGuild(id: String): RevoltGuild? =
        runCatching {
            request<RevoltGuildResponse>("/servers/$id")
        }.getOrNull()?.convert(this)

    override suspend fun getUser(id: String): RevoltUser? =
        runCatching {
            request<RevoltUserResponse>("/users/$id")
        }.getOrNull()?.convert(this)

    override suspend fun getGuildCount(): Int = webSocket.guildCount

    override fun getCustomEmojis(content: String): List<CustomEmoji> = emptyList()

    override fun getMentionedUsers(content: String): List<User> = emptyList()

    override fun getMentionedChannels(content: String): List<Channel> = emptyList()

    override fun getMentionedRoles(content: String): List<Role> = emptyList()

    override fun getEmojiName(typedEmoji: String): String = typedEmoji.removeSurrounding(":")

    override fun emojiAsTyped(emoji: String): String = ":$emoji:"

    override fun getPermissionName(permission: Permission): String = permission.name
}
