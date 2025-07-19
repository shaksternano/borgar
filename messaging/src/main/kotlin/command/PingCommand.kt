package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.messaging.entity.Message
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import java.time.temporal.ChronoUnit

object PingCommand : NonChainableCommand() {

    override val name: String = "ping"
    override val description: String = "Checks the bot's latency."

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
