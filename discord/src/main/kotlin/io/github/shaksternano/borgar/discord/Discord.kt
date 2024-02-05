package io.github.shaksternano.borgar.discord

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import io.github.shaksternano.borgar.chat.command.parseAndExecuteCommand
import io.github.shaksternano.borgar.chat.event.MessageReceiveEvent
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.FileUpload
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun initDiscord(token: String) {
    val jda = default(token, enableCoroutines = true) {
        intents += GatewayIntent.MESSAGE_CONTENT
    }
    DiscordManager.create(jda)
    jda.listener<MessageReceivedEvent> {
        handleMessageEvent(it)
    }
    jda.registerSlashCommands()
    jda.awaitReadySuspend()
}

private suspend fun handleMessageEvent(event: MessageReceivedEvent) = runCatching {
    val convertedEvent = event.convert()
    parseAndExecuteCommand(convertedEvent)
}.onFailure {
    logger.error("Error while handling message event", it)
}

fun MessageReceivedEvent.convert(): MessageReceiveEvent {
    val message = DiscordMessage(message)
    return MessageReceiveEvent(
        message = message,
        manager = message.manager,
    )
}

fun DataSource.toFileUpload(): FileUpload =
    FileUpload.fromStreamSupplier(filename) {
        newStreamBlocking()
    }

private suspend fun JDA.awaitReadySuspend() = suspendCoroutine { continuation ->
    listener<ReadyEvent> {
        continuation.resume(Unit)
    }
}
