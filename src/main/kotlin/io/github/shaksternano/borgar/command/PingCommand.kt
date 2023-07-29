package io.github.shaksternano.borgar.command

import com.google.common.collect.ListMultimap
import io.github.shaksternano.borgar.command.util.CommandResponse
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.time.temporal.ChronoUnit

object PingCommand : KotlinCommand<Unit>(
    "ping",
    "Checks the bot's latency."
) {

    override suspend fun executeSuspend(
        arguments: List<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): CommandResponse<Unit> {
        return CommandResponse("Ping: ...")
    }

    override fun handleFirstResponse(response: Message, event: MessageReceivedEvent, responseData: Unit?) {
        val ping = event.message.timeCreated.until(response.timeCreated, ChronoUnit.MILLIS)
        response.editMessage("Ping: " + ping  + "ms | Websocket: " + event.jda.gatewayPing + "ms").queue()
    }
}
