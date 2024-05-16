package io.github.shaksternano.borgar.messaging.exception

import io.github.shaksternano.borgar.messaging.command.CommandConfig
import io.ktor.http.*
import io.ktor.utils.io.errors.*

class CommandException(
    val commandConfigs: List<CommandConfig>,
    override val message: String = "",
    override val cause: Throwable? = null,
) : Exception(message, cause)

class MissingArgumentException(
    override val message: String,
) : Exception(message)

class HttpException(
    override val message: String,
    val status: HttpStatusCode,
) : IOException(message)

class FileTooLargeException(
    override val cause: Throwable? = null,
) : Exception(cause)
