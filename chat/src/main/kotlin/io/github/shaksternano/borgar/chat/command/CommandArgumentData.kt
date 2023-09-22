package io.github.shaksternano.borgar.chat.command

data class CommandArgumentData(
    val key: String,
    val type: CommandArgumentType,
    val required: Boolean = true,
    val description: String = "",
)
