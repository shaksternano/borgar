package com.shakster.borgar.discord.interaction

import com.shakster.borgar.discord.DiscordManager
import com.shakster.borgar.messaging.command.handleError
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

suspend fun <T : IReplyCallback> handleInteractionCommand(command: DiscordInteractionCommand<T>, event: T) =
    runCatching {
        val responseData = command.respond(event)
        command.onResponseSend(responseData, event)
    }.onFailure {
        val responseContent = handleError(it, DiscordManager[event.jda])
        event.reply(responseContent).await()
    }
