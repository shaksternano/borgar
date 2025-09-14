package com.shakster.borgar.discord.interaction.message

import com.shakster.borgar.core.util.MessagingPlatform
import com.shakster.borgar.discord.entity.DiscordMessage
import com.shakster.borgar.discord.interaction.modal.RunCommandInteractionCommand
import com.shakster.borgar.messaging.util.setSelectedMessage
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.components.label.Label
import net.dv8tion.jda.api.components.textinput.TextInput
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.modals.Modal

object CommandModalInteractionCommand : DiscordMessageInteractionCommand {

    override val name: String = "Run command"

    override suspend fun respond(event: MessageContextInteractionEvent): Any? {
        val command = TextInput.create(RunCommandInteractionCommand.TEXT_INPUT_ID, TextInputStyle.SHORT)
            .setPlaceholder("Enter the command you want to execute on this message")
            .setMinLength(1)
            .build()
        val modal = Modal.create(RunCommandInteractionCommand.name, "name")
            .addComponents(Label.of("Command", command))
            .build()
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
