package io.github.shaksternano.borgar.chat.exception

import io.github.shaksternano.borgar.chat.command.Command

class CommandException(
    val command: Command,
    override val cause: Throwable,
) : Exception(cause)

class MissingArgumentException(
    override val message: String,
) : Exception(message)
