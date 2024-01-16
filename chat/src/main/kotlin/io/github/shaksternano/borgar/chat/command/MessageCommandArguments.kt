package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.core.util.hash
import io.github.shaksternano.borgar.core.util.kClass

class MessageCommandArguments(
    arguments: Map<String, String>,
    defaultArgument: String,
    override val defaultKey: String?,
    private val message: Message,
) : CommandArguments {

    private val arguments: Map<String, String> = buildMap {
        if (defaultKey != null && defaultArgument.isNotBlank()) {
            put(defaultKey, defaultArgument)
        }
        putAll(arguments)
    }

    override fun contains(key: String): Boolean =
        arguments.containsKey(key)

    override fun <T> get(key: String, argumentType: SimpleCommandArgumentType<T>): T? =
        arguments[key]?.let { argumentType.parse(it, message) }

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
