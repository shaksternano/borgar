package com.shakster.borgar.messaging.exception

import com.shakster.borgar.messaging.command.CommandConfig

class CommandException(
    val commandConfigs: List<CommandConfig>,
    override val message: String = "",
    override val cause: Throwable? = null,
) : Exception(message, cause)

class MissingArgumentException(
    override val message: String,
) : Exception(message)
