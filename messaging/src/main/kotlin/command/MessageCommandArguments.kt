package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.util.hash
import io.github.shaksternano.borgar.core.util.kClass
import io.github.shaksternano.borgar.messaging.entity.Message

class MessageCommandArguments(
    arguments: Map<String, String>,
    override val defaultKey: String?,
    defaultArgumentValue: String,
    argumentInfo: Iterable<CommandArgumentInfo<*>>,
    private val message: Message,
) : CommandArguments {

    private val arguments: Map<String, String> = buildMap {
        if (defaultKey != null && defaultArgumentValue.isNotBlank()) {
            this[defaultKey] = defaultArgumentValue
        }
        val argumentNames = argumentInfo.map(CommandArgumentInfo<*>::key).toSet()
        val aliases = argumentInfo.flatMap { argumentInfo ->
            argumentInfo.aliases.map { alias ->
                alias to argumentInfo.key
            }
        }.toMap()
        arguments.forEach { (key, value) ->
            val argumentKey = aliases[key] ?: key
            if (argumentKey in argumentNames) {
                this[argumentKey] = value
            }
        }
    }

    override val typedForm: String = this.arguments.entries.joinToString(" ") { (key, value) ->
        "$ARGUMENT_PREFIX$key $value"
    }

    override fun contains(key: String): Boolean =
        arguments.containsKey(key)

    override fun <T> get(key: String, argumentType: SimpleCommandArgumentType<T>): T? =
        if (argumentType == CommandArgumentType.Attachment) argumentType.parse("", message)
        else arguments[key]?.let { argumentType.parse(it, message) }

    override suspend fun <T> getSuspend(key: String, argumentType: CommandArgumentType<T>): T? = when (argumentType) {
        is SimpleCommandArgumentType -> this[key, argumentType]
        is SuspendingCommandArgumentType -> arguments[key]?.let { argumentType.parse(it, message) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false

        other as MessageCommandArguments

        if (defaultKey != other.defaultKey) return false
        if (message != other.message) return false
        if (arguments != other.arguments) return false

        return true
    }

    override fun hashCode(): Int = hash(
        defaultKey,
        message,
        arguments
    )

    override fun toString(): String {
        return "MessageCommandArguments(" +
            "defaultKey=$defaultKey," +
            "message=$message," +
            "arguments=$arguments" +
            ")"
    }
}
