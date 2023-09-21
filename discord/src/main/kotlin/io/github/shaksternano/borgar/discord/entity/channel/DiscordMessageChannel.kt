package io.github.shaksternano.borgar.discord.entity.channel

import dev.minn.jda.ktx.coroutines.asFlow
import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.chat.builder.MessageCreateBuilder
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateData

data class DiscordMessageChannel(
    private val discordChannel: net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
) : DiscordChannel(discordChannel), MessageChannel {

    override suspend fun sendTyping() {
        discordChannel.sendTyping().await()
    }

    override suspend fun createMessage(block: MessageCreateBuilder.() -> Unit): Message {
        val builder = MessageCreateBuilder().apply(block)
        val message = builder.convert()
        val jdaMessage = discordChannel.sendMessage(message)
            .setMessageReference(builder.referencedMessageId)
            .await()
        return DiscordMessage(jdaMessage)
    }

    override fun getPreviousMessages(beforeId: String, limit: Int): Flow<Message> {
        return discordChannel.iterableHistory.skipTo(beforeId.toLong()).asFlow().map { DiscordMessage(it) }
    }

    private fun MessageCreateBuilder.convert(): MessageCreateData {
        val builder = net.dv8tion.jda.api.utils.messages.MessageCreateBuilder()
        builder.setContent(content)
        builder.addFiles(files.map {
            FileUpload.fromStreamSupplier(it.filename) {
                it.newStreamBlocking()
            }
        })
        return builder.build()
    }
}
