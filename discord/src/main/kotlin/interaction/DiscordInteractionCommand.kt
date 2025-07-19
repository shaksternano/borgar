package io.github.shaksternano.borgar.discord.interaction

import io.github.shaksternano.borgar.core.util.Named
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

interface DiscordInteractionCommand<T : IReplyCallback> : Named {

    val environment: Set<InteractionContextType>
        get() = InteractionContextType.ALL

    suspend fun respond(event: T): Any?

    suspend fun onResponseSend(
        responseData: Any?,
        event: T,
    ) = Unit
}
