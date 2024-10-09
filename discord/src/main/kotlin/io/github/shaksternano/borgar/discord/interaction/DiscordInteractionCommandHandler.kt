package io.github.shaksternano.borgar.discord.interaction

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.messaging.command.handleError
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

suspend fun <T : IReplyCallback> handleInteractionCommand(command: DiscordInteractionCommand<T>, event: T) =
    runCatching {
        val responseData = command.respond(event)
        command.onResponseSend(responseData, event)
    }.onFailure {
        val responseContent = handleError(it, DiscordManager[event.jda])
        event.reply(responseContent).await()
    }
