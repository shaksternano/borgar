package io.github.shaksternano.borgar.revolt.entity

import io.github.shaksternano.borgar.chat.builder.MessageEditBuilder
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.JSON
import io.github.shaksternano.borgar.revolt.RevoltManager
import io.github.shaksternano.borgar.revolt.entity.channel.RevoltChannelResponse
import io.github.shaksternano.borgar.revolt.entity.channel.RevoltMessageChannel
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import ulid.ULID
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class RevoltMessage(
    override val manager: RevoltManager,
    override val id: String,
    override val content: String,
    override val attachments: List<Attachment>,
    override val embeds: List<MessageEmbed>,
    private val authorId: String,
    private val channelId: String,
    private val referencedMessageIds: List<String>,
    private val mentionedUserIds: List<String>,
    private var author: RevoltUser? = null,
    private var authorMember: RevoltMember? = null,
) : BaseEntity(), Message {

    override val timeCreated: OffsetDateTime = run {
        val ulid = ULID.parseULID(id)
        val instant = Instant.ofEpochMilli(ulid.timestamp)
        OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)
    }
    override val customEmojis: Flow<CustomEmoji> = manager.getCustomEmojis(content)
    override val stickers: Flow<Sticker> = emptyFlow()
    override val referencedMessages: Flow<Message> = flow {
        referencedMessageIds.forEach {
            val response = manager.request<RevoltMessageResponse>("/channels/$channelId/messages/$it")
            val referencedMessage = response.convert(manager)
            emit(referencedMessage)
        }
    }

    override val mentionedUsers: Flow<User> = flow {
        for (userId in mentionedUserIds) {
            val user =
                if (userId == authorId) getAuthor()
                else manager.getUser(userId)
            if (user == null) {
                logger.error("User $userId not found")
                continue
            }
            emit(user)
        }
    }
    override val mentionedChannels: Flow<Channel> = emptyFlow()
    override val mentionedRoles: Flow<Role> = emptyFlow()

    private var setAuthorMember: Boolean = authorMember != null
    private lateinit var channel: RevoltMessageChannel

    override suspend fun edit(block: MessageEditBuilder.() -> Unit): Message {
        val builder = MessageEditBuilder().apply(block)
        val requestBody = builder.toRequestBody() ?: return this
        val response = manager.request<RevoltMessageResponse>(
            path = "/channels/$channelId/messages/$id",
            method = HttpMethod.Patch,
            body = requestBody,
        )
        return response.convert(manager)
    }

    override suspend fun delete() =
        manager.request("/channels/$channelId/messages/$id", HttpMethod.Delete)

    override suspend fun getAuthor(): RevoltUser {
        author?.let { return it }
        return manager.getUser(authorId)?.also {
            author = it
        } ?: error("Author not found")
    }

    override suspend fun getAuthorMember(): RevoltMember? {
        if (setAuthorMember) return authorMember
        return getGuild()?.getMember(authorId)?.also {
            authorMember = it
            setAuthorMember = true
        }
    }

    override suspend fun getChannel(): RevoltMessageChannel {
        if (::channel.isInitialized) return channel
        return runCatching {
            manager.request<RevoltChannelResponse>("/channels/$channelId")
        }.getOrElse {
            throw IllegalStateException("Channel $channelId not found", it)
        }.let { body ->
            val user = body.userId?.let { manager.getUser(it) }
            val channel = body.convert(manager, user)
            if (channel !is RevoltMessageChannel) {
                error("Channel $channelId is not a message channel")
            }
            channel.also {
                this.channel = it
            }
        }
    }

    override suspend fun getGuild(): RevoltGuild? =
        getChannel().getGuild()
}

fun createMessage(body: JsonElement, manager: RevoltManager): RevoltMessage =
    JSON.decodeFromJsonElement(RevoltMessageResponse.serializer(), body).convert(manager)

@Serializable
data class RevoltMessageResponse(
    @SerialName("_id")
    val id: String,
    val content: String,
    @SerialName("author")
    val authorId: String,
    @SerialName("channel")
    val channelId: String,
    val attachments: List<RevoltAttachmentBody> = emptyList(),
    val embeds: List<RevoltMessageEmbedBody> = emptyList(),
    @SerialName("replies")
    val referencedMessageIds: List<String> = emptyList(),
    @SerialName("mentions")
    val mentionedUserIds: List<String> = emptyList(),
) {

    fun convert(
        manager: RevoltManager,
        author: RevoltUser? = null,
        authorMember: RevoltMember? = null,
    ): RevoltMessage =
        RevoltMessage(
            manager = manager,
            id = id,
            content = content,
            attachments = attachments.map { it.convert(manager) },
            embeds = embeds.map { it.convert() },
            authorId = authorId,
            channelId = channelId,
            referencedMessageIds = referencedMessageIds,
            mentionedUserIds = mentionedUserIds,
            author = author,
            authorMember = authorMember,
        )
}

@Serializable
data class RevoltAttachmentBody(
    @SerialName("_id")
    val id: String,
    val filename: String,
)

@Serializable
data class RevoltMessageEmbedBody(
    val type: String,
    val url: String? = null,
    val image: RevoltMessageEmbedImageBody? = null,
    val video: RevoltMessageEmbedVideoBody? = null,
) {

    fun convert(): MessageEmbed = when (type) {
        "Website" -> MessageEmbed(
            url = url,
            image = image?.convert(),
            video = video?.convert(),
        )

        "Image" -> MessageEmbed(
            url = url,
            image = url?.let { MessageEmbed.ImageInfo(it, null) },
        )

        "Video" -> MessageEmbed(
            url = url,
            video = url?.let { MessageEmbed.VideoInfo(it, null) },
        )

        else -> MessageEmbed(
            url = url,
        )
    }
}

@Serializable
data class RevoltMessageEmbedImageBody(
    val url: String,
) {

    fun convert(): MessageEmbed.ImageInfo =
        MessageEmbed.ImageInfo(url, null)
}

@Serializable
data class RevoltMessageEmbedVideoBody(
    val url: String,
) {

    fun convert(): MessageEmbed.VideoInfo =
        MessageEmbed.VideoInfo(url, null)
}

private fun RevoltAttachmentBody.convert(manager: RevoltManager): Attachment {
    return Attachment(
        id = id,
        url = "${manager.cdnDomain}/attachments/$id/$filename",
        proxyUrl = null,
        filename = filename,
        manager = manager,
        ephemeral = false,
    )
}

private fun MessageEditBuilder.toRequestBody(): MessageEditRequest? =
    content?.let { MessageEditRequest(it) }

@Serializable
private data class MessageEditRequest(
    val content: String,
)
