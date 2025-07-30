package com.shakster.borgar.messaging.event

import com.shakster.borgar.messaging.BotManager
import com.shakster.borgar.messaging.builder.MessageCreateBuilder
import com.shakster.borgar.messaging.entity.Guild
import com.shakster.borgar.messaging.entity.Member
import com.shakster.borgar.messaging.entity.Message
import com.shakster.borgar.messaging.entity.User
import com.shakster.borgar.messaging.entity.channel.MessageChannel

class MessageReceiveEvent(
    val message: Message,
) : Event {

    override val manager: BotManager = message.manager
    val messageId: String = message.id
    val authorId: String = message.authorId
    val channelId: String = message.channelId

    suspend fun getAuthor(): User = message.getAuthor()

    suspend fun getAuthorMember(): Member? = message.getAuthorMember()

    suspend fun getChannel(): MessageChannel = message.getChannel()
        ?: error("Message channel not found")

    suspend fun getEnvironment() = getChannel().environment

    suspend fun getGuild(): Guild? = message.getGuild()

    suspend fun getGroup() = message.getGroup()

    suspend fun reply(content: String): Message = message.reply(content)

    suspend fun reply(block: MessageCreateBuilder.() -> Unit): Message = message.reply(block)
}
