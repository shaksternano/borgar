package com.shakster.borgar.discord.interaction.message

import com.shakster.borgar.core.util.MessagingPlatform
import com.shakster.borgar.discord.entity.DiscordMessage
import com.shakster.borgar.discord.interaction.modal.RunCommandInteractionCommand
import com.shakster.borgar.messaging.util.setSelectedMessage
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.Modal
import dev.minn.jda.ktx.interactions.components.TextInput
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

object CommandModalInteractionCommand : DiscordMessageInteractionCommand {

    override val name: String = "Run command"

    override suspend fun respond(event: MessageContextInteractionEvent): Any? {
        val command = TextInput(
            id = RunCommandInteractionCommand.TEXT_INPUT_ID,
            label = "Command",
            style = TextInputStyle.SHORT,
        ) {
            placeholder = "Enter the command you want to execute on this message"
            builder.minLength = 1
        }
        val modal = Modal(
            id = RunCommandInteractionCommand.name,
            title = name,
        ) {
            components += ActionRow.of(command)
        }
        val channelId = event.channelId
        if (channelId != null) {
            setSelectedMessage(
                userId = event.user.id,
                channelId = channelId,
                platform = MessagingPlatform.DISCORD,
                message = DiscordMessage(event.target),
            )
        }
        event.replyModal(modal).await()
        return null
    }
}
