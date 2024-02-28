package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.util.asSingletonList
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
