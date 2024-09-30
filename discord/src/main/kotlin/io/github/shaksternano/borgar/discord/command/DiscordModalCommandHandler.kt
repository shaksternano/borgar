package io.github.shaksternano.borgar.discord.command

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.Modal
import dev.minn.jda.ktx.interactions.components.TextInput
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
import io.github.shaksternano.borgar.discord.event.DiscordInteractionCommandEvent
import io.github.shaksternano.borgar.messaging.MessagingPlatform
import io.github.shaksternano.borgar.messaging.command.COMMAND_PREFIX
import io.github.shaksternano.borgar.messaging.command.CommandNotFoundException
import io.github.shaksternano.borgar.messaging.command.isCorrectEnvironment
import io.github.shaksternano.borgar.messaging.command.parseCommands
import io.github.shaksternano.borgar.messaging.entity.FakeMessage
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import io.github.shaksternano.borgar.messaging.executeAndRespond
import io.github.shaksternano.borgar.messaging.util.getAndExpireSelectedMessage
import io.github.shaksternano.borgar.messaging.util.setSelectedMessage
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

suspend fun createCommandModal(event: MessageContextInteractionEvent) {
    val command = TextInput(
        id = "command",
        label = "Command",
        style = TextInputStyle.SHORT,
    ) {
        placeholder = "Enter the command you want to execute on this message"
        builder.minLength = 1
    }
    val modal = Modal(
        id = "command",
        title = "Run command",
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
}

suspend fun handleModalCommand(event: ModalInteractionEvent) {
    val content = event.getValue("command")
        ?.asString
        ?.trim()
        ?.let {
            if (it.startsWith(COMMAND_PREFIX)) {
                it
            } else {
                "$COMMAND_PREFIX$it"
            }
        }
        ?: run {
            logger.error("Command value is null")
            return
        }
    runCatching {
        val channel = DiscordMessageChannel(event.channel)
        val message = FakeMessage(
            id = event.id,
            content = content,
            author = DiscordUser(event.user),
            channel = channel,
        )
        val commandConfigs = try {
            parseCommands(content, message)
        } catch (e: CommandNotFoundException) {
            event.reply("The command **$COMMAND_PREFIX${e.command}** does not exist!")
                .setEphemeral(true)
                .await()
            return
        }
        if (commandConfigs.isEmpty()) {
            event.reply("No commands found!")
                .setEphemeral(true)
                .await()
            return
        }
        val environment = channel.environment
        val firstCommand = commandConfigs.first().command
        if (!firstCommand.isCorrectEnvironment(environment)) {
            event.reply("The command **$COMMAND_PREFIX${firstCommand.name}** cannot be used in a ${environment.displayName.lowercase()} channel!")
                .setEphemeral(true)
                .await()
            return
        }
        val commandEvent = createModalCommandEvent(event)
        commandEvent.executeAndRespond(commandConfigs)
    }.getOrElse {
        logger.error("Error handling modal command $content", it)
        event.reply("An error occurred!")
            .setEphemeral(true)
            .await()
    }
}

private fun createModalCommandEvent(event: ModalInteractionEvent): CommandEvent {
    val discordChannel = event.channel
    // Message interaction event message is not preserved so modal event message is null
    val referencedMessage = getAndExpireSelectedMessage(
        userId = event.user.id,
        channelId = discordChannel.id,
        platform = MessagingPlatform.DISCORD,
    )
    return DiscordInteractionCommandEvent(
        event,
        discordChannel,
        referencedMessage = referencedMessage,
    )
}
