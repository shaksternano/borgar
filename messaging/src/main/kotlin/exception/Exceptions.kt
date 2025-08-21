package com.shakster.borgar.messaging.exception

import com.shakster.borgar.core.util.ChannelEnvironment
import com.shakster.borgar.messaging.command.Command
import com.shakster.borgar.messaging.command.CommandConfig
import com.shakster.borgar.messaging.command.Permission

class CommandException(
    val commandConfigs: List<CommandConfig>,
    override val message: String = "",
    override val cause: Throwable? = null,
) : Exception(message, cause)

class CommandNotFoundException(
    val command: String,
) : Exception()

class NonChainableCommandException(
    commandConfig1: CommandConfig,
    commandConfig2: CommandConfig,
) : Exception() {
    override val message: String = "Cannot chain **${commandConfig1.typedForm}** with **${commandConfig2.typedForm}**!"
}

class TooManyCommandsException() : Exception()

class IncorrectChannelEnvironmentException(
    val command: Command,
    val environment: ChannelEnvironment,
) : Exception()

class InsufficientPermissionsException(
    val command: Command,
    val requiredPermissions: Iterable<Permission>,
) : Exception()

class MissingArgumentException(
    override val message: String,
) : Exception(message)

class InvalidTokenException() : IllegalArgumentException()
