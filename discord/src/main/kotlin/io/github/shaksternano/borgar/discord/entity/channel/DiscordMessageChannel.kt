package io.github.shaksternano.borgar.discord.entity.channel

import dev.minn.jda.ktx.coroutines.asFlow
import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.chat.builder.MessageCreateBuilder
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import io.github.shaksternano.borgar.discord.toFileUpload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.dv8tion.jda.api.utils.messages.MessageCreateData

data class DiscordMessageChannel(
    private val discordMessageChannel: net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
) : DiscordChannel(discordMessageChannel), MessageChannel {

    override suspend fun sendTyping() {
        discordMessageChannel.sendTyping().await()
    }

    override suspend fun createMessage(block: MessageCreateBuilder.() -> Unit): Message {
        val builder = MessageCreateBuilder().apply(block)
        val message = builder.convert()
        val discordMessage = discordMessageChannel.sendMessage(message)
            .setMessageReference(builder.referencedMessageId)
            .setSuppressEmbeds(builder.suppressEmbeds)
            .await()
        return DiscordMessage(discordMessage)
    }

    override fun getPreviousMessages(beforeId: String, limit: Int): Flow<Message> {
        return discordMessageChannel.iterableHistory.skipTo(beforeId.toLong()).asFlow().map { DiscordMessage(it) }
    }

    private fun MessageCreateBuilder.convert(): MessageCreateData {
        val builder = net.dv8tion.jda.api.utils.messages.MessageCreateBuilder()
        builder.setContent(content)
        builder.setFiles(files.map(DataSource::toFileUpload))
        return builder.build()
    }
}
