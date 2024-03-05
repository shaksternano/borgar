package io.github.shaksternano.borgar.revolt.entity.channel

import io.github.shaksternano.borgar.chat.builder.MessageCreateBuilder
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.revolt.RevoltManager
import io.github.shaksternano.borgar.revolt.entity.RevoltMemberBody
import io.github.shaksternano.borgar.revolt.entity.RevoltMessage
import io.github.shaksternano.borgar.revolt.entity.RevoltMessageBody
import io.github.shaksternano.borgar.revolt.entity.RevoltUserBody
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import ulid.ULID

private const val IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"

class RevoltMessageChannel(
    manager: RevoltManager,
    id: String,
    name: String,
    guildId: String?,
) : RevoltChannel(
    manager,
    id,
    name,
    guildId,
), MessageChannel {

    override suspend fun sendTyping() =
        manager.webSocket.sendTyping(id)

    override suspend fun stopTyping() =
        manager.webSocket.stopTyping(id)

    override suspend fun createMessage(block: MessageCreateBuilder.() -> Unit): RevoltMessage {
        val builder = MessageCreateBuilder().apply(block)
        val requestBody = builder.toRequestBody()
        val ulid = ULID.randomULID()
        val response = manager.request<RevoltMessageBody>(
            path = "/channels/$id/messages",
            method = HttpMethod.Post,
            headers = mapOf(
                IDEMPOTENCY_KEY_HEADER to ulid,
            ),
            body = requestBody,
        )
        return response.convert(manager)
    }

    override fun getPreviousMessages(beforeId: String): Flow<Message> = flow {
        var response = requestPreviousMessages(beforeId)
        while (response.messages.isNotEmpty()) {
            val users = response.users
                .map {
                    it.convert(manager)
                }.associateBy {
                    it.id
                }
            val members = response.members
                .mapNotNull { body ->
                    val userId = body.id.user
                    users[userId]?.let {
                        body.convert(manager, it)
                    }
                }.associateBy {
                    it.id
                }
            val messages = response.messages
                .map {
                    val author = users[it.authorId]
                    val authorMember = members[it.authorId]
                    it.convert(manager, author, authorMember)
                }
            messages.forEach {
                emit(it)
            }
            response = requestPreviousMessages(messages.last().id)
        }
    }

    private suspend fun requestPreviousMessages(beforeId: String): RevoltPreviousMessagesBody =
        manager.request<RevoltPreviousMessagesBody>("/channels/$id/messages?before=$beforeId&include_users=true")
}

private fun MessageCreateBuilder.toRequestBody(): MessageCreateRequestBody = MessageCreateRequestBody(
    content = content,
    replies = referencedMessageIds.map { ReplyBody(it, true) },
    masquerade = if (username != null || avatarUrl != null) {
        MasqueradeBody(username, avatarUrl)
    } else {
        null
    },
)

@Serializable
private data class MessageCreateRequestBody(
    val content: String,
    val replies: List<ReplyBody>?,
    val masquerade: MasqueradeBody?,
)

@Serializable
private data class ReplyBody(
    val id: String,
    val mention: Boolean,
)

@Serializable
private data class MasqueradeBody(
    val name: String?,
    val avatar: String?,
)

@Serializable
private data class RevoltPreviousMessagesBody(
    val messages: List<RevoltMessageBody>,
    val users: List<RevoltUserBody>,
    val members: List<RevoltMemberBody> = emptyList(),
)
