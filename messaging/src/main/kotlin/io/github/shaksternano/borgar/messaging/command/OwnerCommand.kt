package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.messaging.event.CommandEvent

abstract class OwnerCommand : NonChainableCommand() {

    override val ownerOnly: Boolean = true

    final override suspend fun run(
        arguments: CommandArguments,
        event: CommandEvent,
    ): List<CommandResponse> {
        return if (event.authorId == event.manager.ownerId) {
            runAsOwner(arguments, event)
        } else {
            listOf(CommandResponse("You don't have permission to use this command."))
        }
    }

    protected abstract suspend fun runAsOwner(
        arguments: CommandArguments,
        event: CommandEvent,
    ): List<CommandResponse>
}
