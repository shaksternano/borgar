package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.util.kClass

data class CommandArgumentInfo<T>(
    val key: String,
    val aliases: Set<String> = emptySet(),
    val description: String = "",
    val type: CommandArgumentType<T>,
    val required: Boolean = true,
    val defaultValue: T? = null,
    val validator: Validator<T> = allowAllValidator(),
    val autoCompleteHandler: CommandAutoCompleteHandler<T>? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false
        other as CommandArgumentInfo<*>
        return key == other.key
    }

    override fun hashCode(): Int = key.hashCode()
}

val CommandArgumentInfo<*>.keyWithPrefix: String
    get() = ARGUMENT_PREFIX + key
