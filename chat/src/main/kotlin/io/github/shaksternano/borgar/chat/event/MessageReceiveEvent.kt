package io.github.shaksternano.borgar.chat.event

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.builder.MessageCreateBuilder
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Member
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel

class MessageReceiveEvent(
    val message: Message,
) : Event {

    override val manager: BotManager = message.manager
    val messageId = message.id
    val authorId = message.authorId

    suspend fun getAuthor(): User = message.getAuthor()

    suspend fun getAuthorMember(): Member? = message.getAuthorMember()

    suspend fun getChannel(): MessageChannel = message.getChannel()

    suspend fun getEnvironment() = getChannel().environment

    suspend fun getGuild(): Guild? = message.getGuild()

    suspend fun getGroup() = message.getGroup()

    suspend fun reply(content: String): Message = message.reply(content)

    suspend fun reply(block: MessageCreateBuilder.() -> Unit): Message = message.reply(block)
}
