package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.asSingletonList
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

object ShutdownCommand : NonChainableCommand() {

    override val name: String = "shutdown"
    override val description: String = "Shuts down the bot. Only the bot owner can use this command."

    override suspend fun run(arguments: CommandArguments, event: CommandEvent): List<CommandResponse> =
        if (event.getAuthor().id == event.manager.ownerId) {
            CommandResponse(
                content = "Shutting down...",
                responseData = true,
            ).also {
                // In case the response fails to send
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch {
                    delay(5000)
                    shutdown()
                }
            }
        } else {
            CommandResponse("You don't have permission to use this command.")
        }.asSingletonList()

    override suspend fun onResponseSend(
        response: CommandResponse,
        responseNumber: Int,
        responseCount: Int,
        sent: Message,
        event: CommandEvent,
    ) {
        if (response.responseData == true) {
            shutdown()
        }
    }

    private fun shutdown() {
        logger.info("Shutdown request received, shutting down...")
        exitProcess(0)
    }
}
