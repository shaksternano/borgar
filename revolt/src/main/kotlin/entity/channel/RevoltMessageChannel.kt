package io.github.shaksternano.borgar.revolt.entity.channel

import io.github.shaksternano.borgar.core.collect.parallelMap
import io.github.shaksternano.borgar.core.exception.FileTooLargeException
import io.github.shaksternano.borgar.core.exception.HttpException
import io.github.shaksternano.borgar.core.io.toChannelProvider
import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.core.util.URL_REGEX
import io.github.shaksternano.borgar.messaging.builder.MessageCreateBuilder
import io.github.shaksternano.borgar.messaging.entity.Message
import io.github.shaksternano.borgar.messaging.entity.channel.MessageChannel
import io.github.shaksternano.borgar.revolt.RevoltManager
import io.github.shaksternano.borgar.revolt.entity.*
import io.github.shaksternano.borgar.revolt.util.RevoltPermissionValue
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ulid.ULID

private const val IDEMPOTENCY_KEY_HEADER: String = "Idempotency-Key"

class RevoltMessageChannel(
    manager: RevoltManager,
    id: String,
    name: String,
    environment: ChannelEnvironment,
    type: RevoltChannelType,
    guildId: String?,
    group: RevoltGroup?,
    defaultPermissions: RevoltPermissionValue?,
    rolePermissions: Map<String, RevoltPermissionValue>,
) : MessageChannel, RevoltChannel(
    manager,
    id,
    name,
    environment,
    type,
    guildId,
    group,
    defaultPermissions,
    rolePermissions,
) {

    override val cancellableTyping: Boolean = true

    override suspend fun sendTyping() =
        manager.webSocket.sendTyping(id)

    override suspend fun stopTyping() =
        manager.webSocket.stopTyping(id)

    override suspend fun createMessage(block: MessageCreateBuilder.() -> Unit): RevoltMessage {
        val builder = MessageCreateBuilder().apply(block)
        require(builder.content.isNotEmpty() || builder.files.isNotEmpty()) {
            "Revolt message content and files cannot both be empty"
        }
        val attachmentIds = builder.uploadAttachments(manager)
        return builder.toRequestBody(attachmentIds).send()
    }

    private suspend fun MessageCreateRequest.send(): RevoltMessage {
        val ulid = ULID.randomULID()
        val response = manager.request<RevoltMessageResponse>(
            path = "/channels/$id/messages",
            method = HttpMethod.Post,
            headers = mapOf(
                IDEMPOTENCY_KEY_HEADER to ulid,
            ),
            body = this,
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
        manager.request<RevoltPreviousMessagesResponse>(
            "/channels/$id/messages?before=$beforeId&include_users=true",
        )
}

private suspend fun MessageCreateBuilder.uploadAttachments(manager: RevoltManager): List<String> =
    files.parallelMap {
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
            throw if (t is HttpException && t.status == HttpStatusCode.PayloadTooLarge) {
                FileTooLargeException(t)
            } else {
                var message = "Failed to upload $filename to Revolt"
                if (t is HttpException) {
                    message += ": ${t.status.value} ${t.status.description}"
                }
                IOException(message, t)
            }
        }.id
    }

private fun MessageCreateBuilder.toRequestBody(attachmentIds: List<String>): MessageCreateRequest =
    MessageCreateRequest(
        content = if (suppressEmbeds) {
            URL_REGEX.findAll(content)
                .toList()
                /*
                Need to reverse the list because inserting tags
                changes the indices of the following matches
                 */
                .reversed()
                .fold(StringBuilder(content)) { newContent, urlMatch ->
                    val range = urlMatch.range
                    /*
                    Need to insert the closing tag first to avoid
                    changing the index of the opening tag
                     */
                    newContent
                        .insert(range.last + 1, ">")
                        .insert(range.first, "<")
                }
                .toString()
        } else {
            content
        }.ifEmpty { null },
        attachments = attachmentIds.ifEmpty { null },
        replies = referencedMessageIds.map {
            ReplyBody(
                id = it,
                mention = true,
                failIfNotExists = false,
            )
        }.ifEmpty { null },
        masquerade = if (username != null || avatarUrl != null) {
            MasqueradeBody(username, avatarUrl)
        } else {
            null
        },
    )

@Serializable
private data class MessageCreateRequest(
    val content: String?,
    val attachments: List<String>?,
    val replies: List<ReplyBody>?,
    val masquerade: MasqueradeBody?,
)

@Serializable
private data class ReplyBody(
    val id: String,
    val mention: Boolean,
    @SerialName("fail_if_not_exists")
    val failIfNotExists: Boolean,
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
