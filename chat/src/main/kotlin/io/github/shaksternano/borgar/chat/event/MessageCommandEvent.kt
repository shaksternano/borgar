package io.github.shaksternano.borgar.chat.event

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.command.CommandArguments
import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
import io.github.shaksternano.borgar.chat.command.CommandResponse
import io.github.shaksternano.borgar.chat.command.getDefaultStringOrEmpty
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel

data class MessageCommandEvent(
    private val event: MessageReceiveEvent,
) : CommandEvent {

    override val id: String = event.messageId
    override val manager: BotManager = event.manager

    private var replied: Boolean = false

    override suspend fun getAuthor(): User = event.getAuthor()

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
            override val content: String = arguments.getDefaultStringOrEmpty()
            override val attachments: List<Attachment> = event.message.attachments
            override val embeds: List<MessageEmbed> = event.message.embeds
            override val customEmojis: List<CustomEmoji> = manager.getCustomEmojis(content)

            override suspend fun getAuthor(): User = this@MessageCommandEvent.getAuthor()

            override suspend fun getChannel(): MessageChannel = this@MessageCommandEvent.getChannel()

            override suspend fun getGuild(): Guild? = this@MessageCommandEvent.getGuild()

            override suspend fun getReferencedMessage(): Message? = this@MessageCommandEvent.getReferencedMessage()
        }
}
