package io.github.shaksternano.borgar.revolt.entity.channel

import io.github.shaksternano.borgar.chat.builder.MessageCreateBuilder
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.core.collect.parallelMap
import io.github.shaksternano.borgar.core.io.toChannelProvider
import io.github.shaksternano.borgar.revolt.RevoltManager
import io.github.shaksternano.borgar.revolt.entity.RevoltMemberResponse
import io.github.shaksternano.borgar.revolt.entity.RevoltMessage
import io.github.shaksternano.borgar.revolt.entity.RevoltMessageResponse
import io.github.shaksternano.borgar.revolt.entity.RevoltUserResponse
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.errors.*
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

    override suspend fun sendCancellableyping() = sendTyping()

    override suspend fun stopTyping() =
        manager.webSocket.stopTyping(id)

    override suspend fun createMessage(block: MessageCreateBuilder.() -> Unit): RevoltMessage {
        val builder = MessageCreateBuilder().apply(block)
        val attachmentIds = builder.files.parallelMap {
            val filename = it.filename
            val channelProvider = it.toChannelProvider()
            val form = formData {
                append("file", channelProvider, headers {
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                })
            }
            runCatching {
                manager.postCdnForm<AttachmentResponse>(
                    path = "/attachments",
                    form = form,
                )
            }.getOrElse { t ->
                throw IOException("Failed to upload $filename to revolt", t)
            }.id
        }
        val requestBody = builder.toRequestBody(attachmentIds)
        val ulid = ULID.randomULID()
        val response = manager.request<RevoltMessageResponse>(
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

    private suspend fun requestPreviousMessages(beforeId: String): RevoltPreviousMessagesResponse =
        manager.request<RevoltPreviousMessagesResponse>("/channels/$id/messages?before=$beforeId&include_users=true")
}

private fun MessageCreateBuilder.toRequestBody(attachmentIds: List<String>?): MessageCreateRequest =
    MessageCreateRequest(
        content = content,
        attachments = attachmentIds,
        replies = referencedMessageIds.map { ReplyBody(it, true) },
        masquerade = if (username != null || avatarUrl != null) {
            MasqueradeBody(username, avatarUrl)
        } else {
            null
        },
    )

@Serializable
private data class MessageCreateRequest(
    val content: String,
    val attachments: List<String>?,
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
private data class AttachmentResponse(
    val id: String,
)

@Serializable
private data class RevoltPreviousMessagesResponse(
    val messages: List<RevoltMessageResponse>,
    val users: List<RevoltUserResponse>,
    val members: List<RevoltMemberResponse> = emptyList(),
)
