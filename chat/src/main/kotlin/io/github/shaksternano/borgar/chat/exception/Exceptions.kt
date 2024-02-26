package io.github.shaksternano.borgar.chat.exception

import io.github.shaksternano.borgar.chat.command.Command

class CommandException(
    val commands: List<Command>,
    override val message: String = "",
    override val cause: Throwable? = null,
) : Exception(message, cause)

class MissingArgumentException(
    override val message: String,
) : Exception(message)
