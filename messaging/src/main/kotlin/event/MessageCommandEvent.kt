package io.github.shaksternano.borgar.messaging.event

import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.command.CommandArguments
import io.github.shaksternano.borgar.messaging.command.CommandMessageIntersection
import io.github.shaksternano.borgar.messaging.command.CommandResponse
import io.github.shaksternano.borgar.messaging.entity.*
import io.github.shaksternano.borgar.messaging.entity.channel.MessageChannel
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

data class MessageCommandEvent(
    private val event: MessageReceiveEvent,
) : CommandEvent {

    override val manager: BotManager = event.manager
    override val id: String = event.messageId
    override val authorId: String = event.authorId
    override val channelId: String = event.channelId
    override val timeCreated: OffsetDateTime = event.message.timeCreated
    override val referencedMessages: Flow<Message> = event.message.referencedMessages
    override var ephemeralReply: Boolean = false
    private var replied: Boolean = false

    override suspend fun getAuthor(): User = event.getAuthor()

    override suspend fun getAuthorMember(): Member? = event.getAuthorMember()

    override suspend fun getChannel(): MessageChannel = event.getChannel()

    override suspend fun getEnvironment(): ChannelEnvironment = event.getEnvironment()

    override suspend fun getGuild(): Guild? = event.getGuild()

    override suspend fun getGroup(): Group? = event.getGroup()

    override suspend fun deferReply() = Unit

    override suspend fun reply(response: CommandResponse): Message = event.getChannel().createMessage {
        fromCommandResponse(response)
        if (!replied) {
            replied = true
            referencedMessageIds.add(event.messageId)
        }
    }

    override fun asMessageIntersection(arguments: CommandArguments): CommandMessageIntersection =
        event.message
}
