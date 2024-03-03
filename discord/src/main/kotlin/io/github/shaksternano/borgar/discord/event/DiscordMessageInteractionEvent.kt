package io.github.shaksternano.borgar.discord.event

import dev.minn.jda.ktx.messages.MessageCreateBuilder
import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.FakeMessage
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.chat.event.MessageInteractionEvent
import io.github.shaksternano.borgar.chat.interaction.MessageInteractionResponse
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.await
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
import io.github.shaksternano.borgar.discord.toFileUpload
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData

class DiscordMessageInteractionEvent(
    private val discordEvent: MessageContextInteractionEvent,
) : MessageInteractionEvent {

    override val manager: BotManager = DiscordManager[discordEvent.jda]
    override val name: String = discordEvent.name
    override val message: Message = DiscordMessage(discordEvent.target)
    override val channel: MessageChannel = DiscordMessageChannel(discordEvent.channel!!)
    override val guild: Guild? = discordEvent.guild?.let { DiscordGuild(it) }

    private var deferred: Boolean = false

    override suspend fun deferReply(ephemeral: Boolean) {
        discordEvent.deferReply(ephemeral).await()
        deferred = true
    }

    override suspend fun reply(response: MessageInteractionResponse): Message {
        val message = response.convert()
        val discordResponse = if (deferred) {
            discordEvent.hook.sendMessage(message)
                .setSuppressEmbeds(response.suppressEmbeds)
                .await()
        } else {
            val interactionHook = discordEvent.reply(message)
                .setEphemeral(response.ephemeral)
                .setSuppressEmbeds(response.suppressEmbeds)
                .await()
            if (response.ephemeral) {
                return FakeMessage(
                    interactionHook.id,
                    manager,
                    response.content,
                    manager.getSelf(),
                    channel,
                )
            }
            interactionHook.retrieveOriginal().await()
        }
        return DiscordMessage(discordResponse)
    }

    private fun MessageInteractionResponse.convert(): MessageCreateData {
        val builder = MessageCreateBuilder(
            content = content,
            files = files.map { it.toFileUpload() }
        )
        return builder.build()
    }
}
