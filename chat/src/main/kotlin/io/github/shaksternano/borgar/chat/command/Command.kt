package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.util.kClass

interface Command {

    val name: String
    val description: String
    val defaultArgumentKey: String?
        get() = null
    val argumentData: Set<CommandArgumentData>
        get() = emptySet()

    suspend fun run(arguments: CommandArguments, event: CommandEvent): Executable
}

val Command.nameWithPrefix: String
    get() = COMMAND_PREFIX + name

interface Executable {

    val command: Command

    suspend fun execute(): List<CommandResponse>

    suspend fun onResponseSend(
        response: CommandResponse,
        responseNumber: Int,
        responseCount: Int,
        sent: Message,
        event: CommandEvent,
    ) = Unit

    suspend fun cleanup() = Unit

    infix fun then(after: Executable): Executable =
        throw UnsupportedOperationException("Cannot chain ${command.name} with ${after.command.name}")
}

abstract class BaseCommand : Command {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false
        other as Command
        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()
}

abstract class NonChainableCommand : BaseCommand() {

    final override suspend fun run(arguments: CommandArguments, event: CommandEvent): Executable = object : Executable {
        override val command: Command = this@NonChainableCommand

        override suspend fun execute(): List<CommandResponse> = runDirect(arguments, event)

        override suspend fun onResponseSend(
            response: CommandResponse,
            responseNumber: Int,
            responseCount: Int,
            sent: Message,
            event: CommandEvent,
        ) = this@NonChainableCommand.onResponseSend(
            response,
            responseNumber,
            responseCount,
            sent,
            event,
        )
    }

    abstract suspend fun runDirect(arguments: CommandArguments, event: CommandEvent): List<CommandResponse>

    open suspend fun onResponseSend(
        response: CommandResponse,
        responseNumber: Int,
        responseCount: Int,
        sent: Message,
        event: CommandEvent,
    ) = Unit
}
