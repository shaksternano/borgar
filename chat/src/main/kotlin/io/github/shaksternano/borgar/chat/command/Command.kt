package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.exception.MissingArgumentException

interface Command {

    val name: String
    val description: String
    val argumentInfo: Set<CommandArgumentInfo<*>>
        get() = emptySet()
    val defaultArgumentKey: String?
        get() = argumentInfo.firstOrNull()?.key
    val guildOnly: Boolean
        get() = false
    val requiredPermissions: Set<Permission>
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

    private val argumentInfoMap: Map<String, CommandArgumentInfo<*>> by lazy {
        argumentInfo.associateBy(CommandArgumentInfo<*>::key)
    }

    private suspend fun <T> getArgument(
        key: String,
        type: CommandArgumentType<T>,
        argumentInfo: CommandArgumentInfo<*>,
        arguments: CommandArguments
    ): ArgumentRetrievalResult<T> {
        if (argumentInfo.type != type) throw IllegalArgumentException("Expected argument type $type for key $key, but got ${argumentInfo.type}")
        @Suppress("UNCHECKED_CAST")
        argumentInfo as CommandArgumentInfo<T>
        val value = arguments.getSuspend(key, type)
        return if (value == null) {
            if (key in arguments) {
                ArgumentRetrievalResult(null, "The argument `$key` is not a `${type.name}`.")
            } else if (argumentInfo.defaultValue == null && argumentInfo.required) {
                ArgumentRetrievalResult(null, "Missing argument `$key`.")
            } else {
                ArgumentRetrievalResult(argumentInfo.defaultValue, "")
            }
        } else {
            val validator = argumentInfo.validator
            if (validator.validate(value)) {
                ArgumentRetrievalResult(value, "")
            } else {
                ArgumentRetrievalResult(
                    null,
                    validator.errorMessage(value, key)
                )
            }
        }
    }

    private suspend fun <T> getArgument(
        key: String,
        type: CommandArgumentType<T>,
        arguments: CommandArguments
    ): ArgumentRetrievalResult<T> {
        val argumentInfo = argumentInfoMap[key] ?: throw IllegalArgumentException("Argument `$key` is not registered.")
        return getArgument(key, type, argumentInfo, arguments)
    }

    protected suspend fun <T> getRequiredArgument(
        key: String,
        type: CommandArgumentType<T>,
        arguments: CommandArguments,
        event: CommandEvent
    ): T {
        val argumentResult = getArgument(key, type, arguments)
        val value = argumentResult.value
        return if (value == null) {
            val errorMessage = argumentResult.errorMessage.ifBlank {
                "Missing argument `$key`."
            }
            throw MissingArgumentException(errorMessage)
        } else {
            val errorMessage = argumentResult.errorMessage
            if (errorMessage.isNotBlank()) {
                event.reply(errorMessage)
            }
            value
        }
    }

    protected suspend fun <T> getOptionalArgument(
        key: String,
        type: CommandArgumentType<T>,
        arguments: CommandArguments,
        event: CommandEvent
    ): T? {
        val argumentResult = getArgument(key, type, arguments)
        if (argumentResult.errorMessage.isNotBlank()) {
            event.reply(argumentResult.errorMessage)
        }
        return argumentResult.value
    }

    override fun toString(): String =
        "Command(" +
            "name='$name'," +
            "description='$description'," +
            "defaultArgumentKey=$defaultArgumentKey," +
            "argumentData=$argumentInfo)"
}

private class ArgumentRetrievalResult<T>(
    val value: T?,
    val errorMessage: String,
)

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
