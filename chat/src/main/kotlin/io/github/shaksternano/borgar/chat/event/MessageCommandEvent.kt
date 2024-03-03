package io.github.shaksternano.borgar.chat.event

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.command.CommandArguments
import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
import io.github.shaksternano.borgar.chat.command.CommandResponse
import io.github.shaksternano.borgar.chat.command.getDefaultStringOrEmpty
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

data class MessageCommandEvent(
    private val event: MessageReceiveEvent,
) : CommandEvent {

    override val id: String = event.messageId
    override val manager: BotManager = event.manager
    override val timeCreated: OffsetDateTime = event.message.timeCreated
    override val referencedMessages: Flow<Message> = event.message.referencedMessages
    override var ephemeralReply: Boolean = false
    private var replied: Boolean = false

    override suspend fun getAuthor(): User = event.getAuthor()

    override suspend fun getMember(): Member? = event.getMember()

    override suspend fun getChannel(): MessageChannel = event.getChannel()

    override suspend fun getGuild(): Guild? = event.getGuild()

    override suspend fun deferReply() = Unit

    override suspend fun reply(response: CommandResponse): Message = event.getChannel().createMessage {
        fromCommandResponse(response)
        referencedMessageId = if (replied) {
            null
        } else {
            replied = true
            event.messageId
        }
    }

    override fun asMessageIntersection(arguments: CommandArguments): CommandMessageIntersection =
        object : CommandMessageIntersection {
            override val id: String = this@MessageCommandEvent.id
            override val manager: BotManager = this@MessageCommandEvent.manager
            override val content: String = arguments.getDefaultStringOrEmpty()
            override val attachments: List<Attachment> = event.message.attachments
            override val embeds: List<MessageEmbed> = event.message.embeds
            override val customEmojis: List<CustomEmoji> = event.message.customEmojis
            override val stickers: List<Sticker> = event.message.stickers
            override val referencedMessages: Flow<Message> = this@MessageCommandEvent.referencedMessages

            override suspend fun getAuthor(): User = this@MessageCommandEvent.getAuthor()

            override suspend fun getMember(): Member? = this@MessageCommandEvent.getMember()

            override suspend fun getChannel(): MessageChannel = this@MessageCommandEvent.getChannel()

            override suspend fun getGuild(): Guild? = this@MessageCommandEvent.getGuild()
        }
}
