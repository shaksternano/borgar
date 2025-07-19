package io.github.shaksternano.borgar.messaging.exception

import io.github.shaksternano.borgar.messaging.command.CommandConfig

class CommandException(
    val commandConfigs: List<CommandConfig>,
    override val message: String = "",
    override val cause: Throwable? = null,
) : Exception(message, cause)

class MissingArgumentException(
    override val message: String,
) : Exception(message)
