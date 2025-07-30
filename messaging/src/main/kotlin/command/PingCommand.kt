package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.util.asSingletonList
import com.shakster.borgar.messaging.entity.Message
import com.shakster.borgar.messaging.event.CommandEvent
import java.time.temporal.ChronoUnit

object PingCommand : NonChainableCommand() {

    override val name: String = "ping"
    override val description: String = "Checks the bot's latency."
    override val ephemeralReply: Boolean = true

    override suspend fun run(arguments: CommandArguments, event: CommandEvent): List<CommandResponse> =
        CommandResponse("Ping: ...").asSingletonList()

    override suspend fun onResponseSend(
        response: CommandResponse,
        responseNumber: Int,
        responseCount: Int,
        sent: Message,
        event: CommandEvent,
    ) {
        val ping = event.timeCreated.until(sent.timeCreated, ChronoUnit.MILLIS)
        sent.edit("Ping: ${ping}ms")
    }
}
