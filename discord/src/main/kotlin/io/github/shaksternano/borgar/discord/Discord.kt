package io.github.shaksternano.borgar.discord

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import io.github.shaksternano.borgar.chat.command.HelpCommand
import io.github.shaksternano.borgar.chat.command.parseAndExecuteCommand
import io.github.shaksternano.borgar.chat.event.MessageReceiveEvent
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import kotlin.time.Duration

fun initDiscord(token: String) {
    val jda = default(token, enableCoroutines = true) {
        intents += GatewayIntent.MESSAGE_CONTENT
    }
    jda.listener<MessageReceivedEvent> {
        handleMessageEvent(it)
    }
    jda.initCommands()
    DiscordManager.create(jda)
}

private suspend fun handleMessageEvent(event: MessageReceivedEvent) {
    val convertedEvent = event.convert()
    parseAndExecuteCommand(convertedEvent)
}

fun MessageReceivedEvent.convert(): MessageReceiveEvent {
    val message = DiscordMessage(message)
    return MessageReceiveEvent(
        message = message,
        manager = message.manager,
    )
}

private fun JDA.initCommands() {
    updateCommands {
        slash(HelpCommand.name, HelpCommand.description)
    }
    onSlashCommand(HelpCommand.name) {
        handleHelpCommand(it)
    }
}

private fun JDA.onSlashCommand(
    name: String,
    timeout: Duration? = null,
    consumer: suspend CoroutineEventListener.(SlashCommandInteractionEvent) -> Unit
) = listener<SlashCommandInteractionEvent>(timeout) {
    if (it.name == name) {
        consumer(it)
    }
}

private suspend fun handleHelpCommand(event: SlashCommandInteractionEvent) {
    val entityId = event.guild?.id ?: event.user.id
    val helpMessages = HelpCommand.getHelpMessages(entityId, Message.MAX_CONTENT_LENGTH)
    helpMessages.forEachIndexed { index, message ->
        if (index == 0) {
            event.reply(message)
                .setSuppressEmbeds(true)
                .await()
        } else {
            event.messageChannel.sendMessage(message)
                .setSuppressEmbeds(true)
                .await()
        }
    }
}
