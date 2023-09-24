package io.github.shaksternano.borgar.chat.event

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.command.CommandArguments
import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
import io.github.shaksternano.borgar.chat.command.CommandResponse
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel

data class MessageCommandEvent(
    private val event: MessageReceiveEvent,
) : CommandEvent {

    override val id: String = event.messageId
    override val manager: BotManager = event.manager

    private var replied: Boolean = false

    override suspend fun getUser(): User = event.getAuthor()

    override suspend fun getChannel(): MessageChannel = event.getChannel()

    override suspend fun getGuild(): Guild? = event.getGuild()

    override suspend fun getReferencedMessage(): Message? = event.message.getReferencedMessage()

    override suspend fun respond(response: CommandResponse): Message = event.getChannel().createMessage {
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
            override val content: String = arguments.defaultKey?.let(arguments::getString) ?: ""
            override val attachments: List<Attachment> = event.message.attachments
            override val embeds: List<MessageEmbed> = event.message.embeds

            override suspend fun getUser(): User = this@MessageCommandEvent.getUser()

            override suspend fun getChannel(): MessageChannel = this@MessageCommandEvent.getChannel()

            override suspend fun getGuild(): Guild? = this@MessageCommandEvent.getGuild()

            override suspend fun getReferencedMessage(): Message? = this@MessageCommandEvent.getReferencedMessage()
        }
}