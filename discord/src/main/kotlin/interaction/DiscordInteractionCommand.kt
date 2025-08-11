package com.shakster.borgar.discord.interaction

import com.shakster.borgar.messaging.command.RegisterableCommand
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

interface DiscordInteractionCommand<T : IReplyCallback> : RegisterableCommand {

    val environment: Set<InteractionContextType>
        get() = InteractionContextType.ALL

    suspend fun respond(event: T): Any?

    suspend fun onResponseSend(
        responseData: Any?,
        event: T,
    ) = Unit
}
