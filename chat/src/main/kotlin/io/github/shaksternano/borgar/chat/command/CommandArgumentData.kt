package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.core.util.kClass

data class CommandArgumentData(
    val key: String,
    val description: String = "",
    val type: CommandArgumentType,
    val required: Boolean = true,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false
        other as CommandArgumentData
        return key == other.key
    }

    override fun hashCode(): Int = key.hashCode()
}
