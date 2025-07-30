package com.shakster.borgar.discord.interaction.message

import com.shakster.borgar.core.util.MessagingPlatform
import com.shakster.borgar.discord.entity.DiscordMessage
import com.shakster.borgar.messaging.util.setSelectedMessage
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

object SelectMessageInteractionCommand : DiscordMessageInteractionCommand {

    override val name: String = "Select message"

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
