package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.exception.MissingArgumentException
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.util.startsWithVowel

interface Command {

    val name: String
    val aliases: Set<String>
        get() = emptySet()
    val description: String
    val argumentInfo: Set<CommandArgumentInfo<*>>
        get() = emptySet()
    val defaultArgumentKey: String?
        get() = argumentInfo.firstOrNull()?.key
    val chainable: Boolean
    val guildOnly: Boolean
        get() = false
    val requiredPermissions: Set<Permission>
        get() = emptySet()
    val deferReply: Boolean
    val ephemeral: Boolean
        get() = false
    val entityId: String?
        get() = null

    suspend fun run(arguments: CommandArguments, event: CommandEvent): Executable
}

val Command.nameWithPrefix: String
    get() = COMMAND_PREFIX + name

interface Executable : SuspendCloseable {

    val command: Command

    suspend fun execute(): List<CommandResponse>

    suspend fun onResponseSend(
        response: CommandResponse,
        responseNumber: Int,
        responseCount: Int,
        sent: Message,
        event: CommandEvent,
    ) = Unit

    infix fun then(after: Executable): Executable =
        throw UnsupportedOperationException("Cannot chain ${command.name} with ${after.command.name}")

    override suspend fun close() = Unit
}

abstract class BaseCommand : Command {

    private val argumentInfoMap: Map<String, CommandArgumentInfo<*>> by lazy {
        argumentInfo.associateBy(CommandArgumentInfo<*>::key)
    }

    protected suspend fun <T> CommandArguments.getRequired(
        key: String,
        type: CommandArgumentType<T>,
    ): T {
        val argumentResult = getArgument(key, type)
        val value = argumentResult.value
        return if (value == null) {
            val errorMessage = argumentResult.errorMessage.ifBlank {
                "Missing argument `$key`."
            }
            throw MissingArgumentException(errorMessage)
        } else {
            val errorMessage = argumentResult.errorMessage
            if (errorMessage.isNotBlank()) throw MissingArgumentException(errorMessage)
            else value
        }
    }

    protected suspend fun <T> CommandArguments.getOptional(
        key: String,
        type: CommandArgumentType<T>,
    ): T? {
        val argumentResult = getArgument(key, type)
        return if (argumentResult.errorMessage.isNotBlank())
            throw MissingArgumentException(argumentResult.errorMessage)
        else argumentResult.value
    }

    private suspend fun <T> CommandArguments.getArgument(
        key: String,
        type: CommandArgumentType<T>,
    ): ArgumentRetrievalResult<T> {
        val argumentInfo = argumentInfoMap[key] ?: throw IllegalArgumentException("Argument `$key` is not registered.")
        return getArgument(key, type, argumentInfo)
    }

    private suspend fun <T> CommandArguments.getArgument(
        key: String,
        type: CommandArgumentType<T>,
        argumentInfo: CommandArgumentInfo<*>,
    ): ArgumentRetrievalResult<T> {
        if (argumentInfo.type != type)
            throw IllegalArgumentException("Expected argument type $type for key $key, but got ${argumentInfo.type}")
        @Suppress("UNCHECKED_CAST")
        argumentInfo as CommandArgumentInfo<T>
        val value = getSuspend(key, type)
        return if (value == null) {
            if (key in this) {
                var errorMessage = "The argument **$key** is not a"
                if (type.name.startsWithVowel()) {
                    errorMessage += "n"
                }
                errorMessage += " ${type.name}."
                ArgumentRetrievalResult(null, errorMessage)
            } else if (argumentInfo.defaultValue == null && argumentInfo.required) {
                ArgumentRetrievalResult(null, "Missing argument **$key**.")
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

    override fun toString(): String =
        "Command(" +
            "name='$name'," +
            "aliases=$aliases," +
            "description='$description'," +
            "argumentInfo=$argumentInfo," +
            "defaultArgumentKey=$defaultArgumentKey," +
            "guildOnly=$guildOnly," +
            "requiredPermissions=$requiredPermissions," +
            "entityId=$entityId" +
            ")"
}

private class ArgumentRetrievalResult<T>(
    val value: T?,
    val errorMessage: String,
)

abstract class NonChainableCommand : BaseCommand() {

    override val chainable: Boolean = false
    override val deferReply: Boolean = false

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
