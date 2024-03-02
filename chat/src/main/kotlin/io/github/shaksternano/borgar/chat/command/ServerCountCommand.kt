package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.util.asSingletonList

object ServerCountCommand : NonChainableCommand() {

    override val name: String = "servercount"
    override val aliases: Set<String> = setOf("servers")
    override val description: String = "Gets the number of servers that this bot is in."

    override suspend fun run(arguments: CommandArguments, event: CommandEvent): List<CommandResponse> {
        val guildCount = event.manager.getGuildCount()
        var message = "This bot is in $guildCount server"
        if (guildCount != 1) {
            message += "s"
        }
        return CommandResponse(message).asSingletonList()
    }
}
