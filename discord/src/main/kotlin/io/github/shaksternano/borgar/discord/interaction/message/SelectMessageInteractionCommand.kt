package io.github.shaksternano.borgar.discord.interaction.message

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import io.github.shaksternano.borgar.messaging.MessagingPlatform
import io.github.shaksternano.borgar.messaging.util.setSelectedMessage
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

object SelectMessageInteractionCommand : DiscordMessageInteractionCommand {

    override val name: String = "Select Message"

    override suspend fun respond(event: MessageContextInteractionEvent): Any? {
        val message = event.target
        setSelectedMessage(
            userId = event.user.id,
            channelId = event.target.channelId,
            platform = MessagingPlatform.DISCORD,
            message = DiscordMessage(event.target),
        )
        event.reply("The message ${message.jumpUrl} has been selected for your next command.")
            .setEphemeral(true)
            .await()
        return null
    }
}
