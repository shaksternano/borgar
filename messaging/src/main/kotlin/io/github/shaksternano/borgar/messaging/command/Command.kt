package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.core.util.Named
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.startsWithVowel
import io.github.shaksternano.borgar.messaging.entity.Message
import io.github.shaksternano.borgar.messaging.entity.User
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import io.github.shaksternano.borgar.messaging.exception.MissingArgumentException
import kotlinx.coroutines.flow.firstOrNull

interface Command : Named {

    val aliases: Set<String>
        get() = emptySet()
    val description: String
    val argumentInfo: Set<CommandArgumentInfo<*>>
        get() = emptySet()
    val defaultArgumentKey: String?
        get() = argumentInfo.firstOrNull()?.key
    val chainable: Boolean
    val environment: Set<ChannelEnvironment>
        get() = ChannelEnvironment.ALL
    val requiredPermissions: Set<Permission>
        get() = emptySet()
    val deferReply: Boolean
    val ephemeralReply: Boolean
        get() = false
    val entityId: String?
        get() = null
    val entityEnvironment: ChannelEnvironment?
        get() = null

    fun createExecutable(arguments: CommandArguments, event: CommandEvent): Executable
}

val Command.nameWithPrefix: String
    get() = COMMAND_PREFIX + name

val Command.guildOnly: Boolean
    get() = environment.let {
        it.size == 1 && it.contains(ChannelEnvironment.GUILD)
    }

fun Command.isCorrectEnvironment(environment: ChannelEnvironment): Boolean =
    environment in this.environment

interface Executable : SuspendCloseable {

    /**
     * The commands that produced this executable.
     * Should always have at least one element.
     */
    val commandConfigs: List<CommandConfig>

    suspend fun run(): List<CommandResponse>

    suspend fun onResponseSend(
        response: CommandResponse,
        responseNumber: Int,
        responseCount: Int,
        sent: Message,
        event: CommandEvent,
    ) = Unit

    infix fun then(after: Executable): Executable =
        throw NonChainableCommandException(commandConfigs.last(), commandConfigs.first())

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
        val argumentResult = getArgument(key, type, false)
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
        val argumentResult = getArgument(key, type, true)
        return if (argumentResult.errorMessage.isNotBlank())
            throw MissingArgumentException(argumentResult.errorMessage)
        else argumentResult.value
    }

    private suspend fun <T> CommandArguments.getArgument(
        key: String,
        type: CommandArgumentType<T>,
        optional: Boolean,
    ): ArgumentRetrievalResult<T> {
        val argumentInfo = argumentInfoMap[key] ?: throw IllegalArgumentException("Argument `$key` is not registered.")
        return getArgument(key, type, argumentInfo, optional)
    }

    private suspend fun <T> CommandArguments.getArgument(
        key: String,
        type: CommandArgumentType<T>,
        argumentInfo: CommandArgumentInfo<*>,
        optional: Boolean,
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
                val error = if (optional) "" else "Missing argument **$key**."
                ArgumentRetrievalResult(null, error)
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

    protected suspend fun getReferencedUser(arguments: CommandArguments, event: CommandEvent): User {
        val argumentUser = arguments.getOptional("user", CommandArgumentType.User)
        return argumentUser ?: run {
            val messageIntersection = event.asMessageIntersection(arguments)
            val referencedMessage = messageIntersection.referencedMessages.firstOrNull()
            val referencedUser = referencedMessage?.getAuthor()
            referencedUser ?: event.getAuthor()
        }
    }

    override fun toString(): String =
        "Command(" +
            "name='$name'," +
            "aliases=$aliases," +
            "description='$description'," +
            "argumentInfo=$argumentInfo," +
            "defaultArgumentKey=$defaultArgumentKey," +
            "environment=$environment," +
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

    final override fun createExecutable(arguments: CommandArguments, event: CommandEvent): Executable =
        object : Executable {

            override val commandConfigs: List<CommandConfig> =
                CommandConfig(this@NonChainableCommand, arguments).asSingletonList()

            override suspend fun run(): List<CommandResponse> = run(arguments, event)

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

    abstract suspend fun run(arguments: CommandArguments, event: CommandEvent): List<CommandResponse>

    open suspend fun onResponseSend(
        response: CommandResponse,
        responseNumber: Int,
        responseCount: Int,
        sent: Message,
        event: CommandEvent,
    ) = Unit
}
