package io.github.shaksternano.borgar.chat.exception

import io.github.shaksternano.borgar.chat.command.CommandConfig

class CommandException(
    val commands: List<CommandConfig>,
    override val message: String = "",
    override val cause: Throwable? = null,
) : Exception(message, cause)

class MissingArgumentException(
    override val message: String,
) : Exception(message)
