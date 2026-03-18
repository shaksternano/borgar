package com.shakster.borgar.stoat.entity

import com.shakster.borgar.core.logger
import com.shakster.borgar.core.util.JSON
import com.shakster.borgar.core.util.encodeUrl
import com.shakster.borgar.messaging.builder.MessageEditBuilder
import com.shakster.borgar.messaging.entity.*
import com.shakster.borgar.messaging.entity.channel.Channel
import com.shakster.borgar.stoat.StoatManager
import com.shakster.borgar.stoat.USER_SILENT_MENTION_REGEX
import com.shakster.borgar.stoat.entity.channel.StoatMessageChannel
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

data class StoatMessage(
    override val manager: StoatManager,
    override val id: String,
    override val authorId: String,
    override val channelId: String,
    override val content: String,
    override val attachments: List<Attachment>,
    private val embeds: List<MessageEmbed>,
    private val referencedMessageIds: List<String>,
    private val mentionedUserIds: List<String>,
    private var author: StoatUser? = null,
    private var authorMember: StoatMember? = null,
) : Message, BaseEntity() {

    override val timeCreated: OffsetDateTime = run {
        val ulid = ULID.parseULID(id)
        val instant = Instant.ofEpochMilli(ulid.timestamp)
        OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)
    }
    override val customEmojis: Flow<CustomEmoji> = manager.getCustomEmojis(content)
    override val stickers: Flow<Sticker> = emptyFlow()
    override val referencedMessages: Flow<Message> = flow {
        referencedMessageIds.forEach {
            val response = manager.request<StoatMessageResponse>("/channels/$channelId/messages/$it")
            val referencedMessage = response.convert(manager)
            emit(referencedMessage)
        }
    }

    override val mentionedUsers: Flow<User> = flow {
        val matches = USER_SILENT_MENTION_REGEX.findAll(content)
        val userIds = mentionedUserIds + matches.map { match ->
            match.value.removeSurrounding("<\\@", ">")
        }
        for (userId in userIds) {
            val user =
                if (userId == authorId) getAuthor()
                else manager.getUser(userId)
            if (user == null) {
                logger.error("Stoat user $userId not found")
                continue
            }
            emit(user)
        }
    }
    override val mentionedChannels: Flow<Channel> = emptyFlow()
    override val mentionedRoles: Flow<Role> = emptyFlow()

    override val link: String = "${manager.appUrl}/channel/$channelId/$id"

    private var setAuthorMember: Boolean = authorMember != null
    private lateinit var channel: StoatMessageChannel

    override suspend fun edit(block: MessageEditBuilder.() -> Unit): Message {
        val builder = MessageEditBuilder().apply(block)
        val requestBody = builder.toRequestBody() ?: return this
        val response = manager.request<StoatMessageResponse>(
            path = "/channels/$channelId/messages/$id",
            method = HttpMethod.Patch,
            body = requestBody,
        )
        return response.convert(manager)
    }

    override suspend fun delete() =
        manager.request<Unit>("/channels/$channelId/messages/$id", HttpMethod.Delete)

    override suspend fun getAuthor(): StoatUser {
        author?.let { return it }
        return manager.getUser(authorId)?.also {
            author = it
        } ?: error("Stoat message author $authorId not found")
    }

    override suspend fun getAuthorMember(): StoatMember? {
        if (setAuthorMember) return authorMember
        return getGuild()?.getMember(authorId)?.also {
            authorMember = it
            setAuthorMember = true
        }
    }

    override suspend fun getChannel(): StoatMessageChannel {
        if (::channel.isInitialized) return channel
        val channel = manager.getChannel(channelId) ?: error("Channel $channelId not found")
        if (channel !is StoatMessageChannel) {
            error("Stoat channel $channelId is not a message channel, but a ${channel.type.apiName}")
        }
        this.channel = channel
        return channel
    }

    override suspend fun getGuild(): StoatGuild? =
        getChannel().getGuild()

    override suspend fun getGroup(): StoatGroup? =
        getChannel().getGroup()

    override suspend fun getEmbeds(): List<MessageEmbed> = embeds
}

fun createMessage(body: JsonElement, manager: StoatManager): StoatMessage =
    JSON.decodeFromJsonElement(StoatMessageResponse.serializer(), body).convert(manager)

@Serializable
data class StoatMessageResponse(
    @SerialName("_id")
    val id: String,
    val content: String = "",
    @SerialName("author")
    val authorId: String,
    @SerialName("channel")
    val channelId: String,
    val attachments: List<StoatAttachmentBody> = emptyList(),
    val embeds: List<StoatMessageEmbedBody> = emptyList(),
    @SerialName("replies")
    val referencedMessageIds: List<String> = emptyList(),
    @SerialName("mentions")
    val mentionedUserIds: List<String> = emptyList(),
) {

    fun convert(
        manager: StoatManager,
        author: StoatUser? = null,
        authorMember: StoatMember? = null,
    ): StoatMessage =
        StoatMessage(
            manager = manager,
            id = id,
            authorId = authorId,
            channelId = channelId,
            content = content,
            attachments = attachments.map { it.convert(manager) },
            embeds = embeds.map { it.convert(manager) },
            referencedMessageIds = referencedMessageIds,
            mentionedUserIds = mentionedUserIds,
            author = author,
            authorMember = authorMember,
        )
}

@Serializable
data class StoatAttachmentBody(
    @SerialName("_id")
    val id: String,
    val filename: String,
)

@Serializable
data class StoatMessageEmbedBody(
    val type: String,
    val url: String? = null,
    val image: StoatMessageEmbedImageBody? = null,
    val video: StoatMessageEmbedVideoBody? = null,
) {

    fun convert(manager: StoatManager): MessageEmbed = when (type) {
        "Website" -> MessageEmbed(
            url = url,
            image = image?.convert(manager),
            video = video?.convert(manager),
        )

        "Image" -> MessageEmbed(
            url = url,
            image = url?.toImageInfo(manager),
        )

        "Video" -> MessageEmbed(
            url = url,
            video = url?.toVideoInfo(manager),
        )

        else -> MessageEmbed(
            url = url,
        )
    }
}

@Serializable
data class StoatMessageEmbedImageBody(
    val url: String,
) {

    fun convert(manager: StoatManager): MessageEmbed.ImageInfo =
        url.toImageInfo(manager)
}

@Serializable
data class StoatMessageEmbedVideoBody(
    val url: String,
) {

    fun convert(manager: StoatManager): MessageEmbed.VideoInfo =
        url.toVideoInfo(manager)
}

private fun String.toImageInfo(manager: StoatManager): MessageEmbed.ImageInfo =
    MessageEmbed.ImageInfo(this, toProxyUrl(manager))

private fun String.toVideoInfo(manager: StoatManager): MessageEmbed.VideoInfo =
    MessageEmbed.VideoInfo(this, toProxyUrl(manager))

private fun String.toProxyUrl(manager: StoatManager): String =
    "${manager.proxyUrl}/proxy?url=${encodeUrl()}"

private fun StoatAttachmentBody.convert(manager: StoatManager): Attachment {
    return Attachment(
        id = id,
        url = "${manager.cdnUrl}/attachments/$id/$filename",
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
