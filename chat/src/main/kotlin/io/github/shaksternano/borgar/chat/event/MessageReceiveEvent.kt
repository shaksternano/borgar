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
    override val manager: BotManager,
) : Event {

    val messageId = message.id

    suspend fun getAuthor(): User = message.getAuthor()

    suspend fun getMember(): Member? = message.getMember()

    suspend fun getChannel(): MessageChannel = message.getChannel()

    suspend fun getGuild(): Guild? = message.getGuild()

    suspend fun reply(content: String): Message = message.reply(content)

    suspend fun reply(block: MessageCreateBuilder.() -> Unit): Message = message.reply(block)
}
