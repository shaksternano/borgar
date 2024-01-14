package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.DisplayedUser
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.event.MessageCommandEvent
import io.github.shaksternano.borgar.chat.event.MessageReceiveEvent
import io.github.shaksternano.borgar.chat.exception.CommandException
import io.github.shaksternano.borgar.core.data.repository.TemplateRepository
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.endOfWord
import io.github.shaksternano.borgar.core.util.indexesOfPrefix
import io.github.shaksternano.borgar.core.util.split
import kotlinx.coroutines.flow.fold
import java.util.regex.Matcher
import java.util.regex.Pattern

suspend fun parseAndExecuteCommand(event: MessageReceiveEvent) {
    val contentStripped = contentStripped(event.message).trim()
    if (!contentStripped.startsWith(COMMAND_PREFIX)) return
    val commandConfigs = parseCommands(event.message)
    if (commandConfigs.isEmpty()) return
    val channel = event.getChannel()
    try {
        channel.sendTyping()
    } catch (t: Throwable) {
        logger.error("Failed to send typing indicator", t)
    }
    val commandEvent = MessageCommandEvent(event)
    val (responses, executable) = try {
        val executables = commandConfigs.mapIndexed { i, (command, arguments) ->
            val guild = event.getGuild()
            if (guild == null && command.guildOnly) return
            if (guild != null) {
                val requiredPermissions = command.requiredPermissions
                val permissionHolder = guild.getMember(event.getAuthor()) ?: guild.getPublicRole()
                val hasPermission = permissionHolder.hasPermission(requiredPermissions, channel)
                if (!hasPermission) {
                    if (i == 0) return
                    else throw InsufficientPermissionsException(command, requiredPermissions)
                }
            }

            try {
                command.run(arguments, commandEvent)
            } catch (t: Throwable) {
                throw CommandException(command, t)
            }
        }
        val chained = executables.reduce { executable1, executable2 ->
            try {
                executable1 then executable2
            } catch (e: UnsupportedOperationException) {
                throw NonChainableCommandException(executable1.command, executable2.command, e)
            }
        }
        val result = try {
            chained.execute()
        } catch (t: Throwable) {
            throw CommandException(chained.command, t)
        }
        result to chained
    } catch (t: Throwable) {
        val responseContent = handleError(t)
        listOf(CommandResponse(responseContent)) to null
    }
    sendResponse(responses, executable, commandEvent)
}

suspend fun sendResponse(responses: List<CommandResponse>, executable: Executable?, commandEvent: CommandEvent) {
    responses.forEachIndexed { index, response ->
        try {
            val sent = commandEvent.respond(response)
            try {
                executable?.onResponseSend(
                    response,
                    index + 1,
                    responses.size,
                    sent,
                    commandEvent
                )
            } catch (t: Throwable) {
                logger.error("An error occurred", t)
                commandEvent.respond("An error occurred!")
            }
        } catch (t: Throwable) {
            logger.error("Failed to send response", t)
        }
    }
    executable?.cleanup()
}

private suspend fun contentStripped(message: Message): String {
    var stripped = message.content
    stripped = message.mentionedUsers.fold(stripped) { content, user ->
        val details = userDetails(user, message.getGuild())
        content.replace(
            "<@!?${Pattern.quote(user.id)}>".toRegex(),
            "@${Matcher.quoteReplacement(details.effectiveName)}"
        )
    }
    stripped = message.mentionedChannels.fold(stripped) { content, channel ->
        content.replace(channel.asMention, "#${channel.name}")
    }
    stripped = message.mentionedRoles.fold(stripped) { content, role ->
        content.replace(role.asMention, "@${role.name}")
    }
    return stripped
}

private fun handleError(throwable: Throwable): String = when (throwable) {
    is NonChainableCommandException ->
        "Cannot chain ${throwable.command1.name} with ${throwable.command2.name}!"

    is CommandException -> {
        logger.error("Error executing command ${throwable.command.name}", throwable)
        "An error occurred!"
    }

    is InsufficientPermissionsException ->
        "Command ${throwable.command.nameWithPrefix} requires permissions:\n${
            throwable.requiredPermissions.joinToString(
                "\n"
            ) { it.displayedName }
        }!"

    else -> {
        logger.error("An error occurred", throwable)
        "An error occurred!"
    }
}

private suspend fun userDetails(user: User, guild: Guild?): DisplayedUser {
    return guild?.getMember(user)?.user ?: user
}

private suspend fun parseCommands(message: Message): List<CommandConfig> {
    return parseRawCommands(message.content)
        .mapIndexed { index, (commandString, rawArguments, defaultArgument) ->
            val command = COMMANDS[commandString] ?: getCustomTemplateCommand(
                commandString,
                message
            )
            if (index > 0 && command == null) {
                throw CommandNotFoundException()
            } else if (command == null) {
                return emptyList()
            }
            val arguments = MessageCommandArguments(
                rawArguments,
                defaultArgument,
                command.defaultArgumentKey,
                message
            )
            CommandConfig(command, arguments)
        }
}

internal fun parseRawCommands(message: String): List<RawCommandConfig> {
    return parseCommandStrings(message).map { commandString ->
        val commandEndIndex = commandString.endOfWord(COMMAND_PREFIX.length)
        val command = commandString.substring(COMMAND_PREFIX.length, commandEndIndex)
        val argumentPrefixIndexes = commandString.indexesOfPrefix(ARGUMENT_PREFIX)
        val arguments = commandString.split(argumentPrefixIndexes)
            .associate {
                val argumentNameEndIndex = it.endOfWord(ARGUMENT_PREFIX.length)
                val argumentName = it.substring(ARGUMENT_PREFIX.length, argumentNameEndIndex)
                val argumentValue = it.substring(argumentNameEndIndex).trim()
                argumentName to argumentValue
            }
        val defaultArgument = if (argumentPrefixIndexes.isEmpty()) {
            commandString.substring(commandEndIndex)
        } else {
            commandString.substring(commandEndIndex, argumentPrefixIndexes[0])
        }.trim()
        RawCommandConfig(command, arguments, defaultArgument)
    }
}

internal fun parseCommandStrings(message: String): List<String> {
    val commandPrefixIndexes = message.indexesOfPrefix(COMMAND_PREFIX)
    return message.split(commandPrefixIndexes).map { it.trim() }
}

private suspend fun getCustomTemplateCommand(commandName: String, message: Message): Command? {
    val commandNameParts = commandName.split(ENTITY_ID_SEPARATOR, limit = 2)
    val templateName = commandNameParts[0]
    val entityId = if (commandNameParts.size == 1) {
        message.getGuild()?.id ?: message.getAuthor().id
    } else {
        val entityId = commandNameParts[1]
        if (message.getAuthor().id == entityId) {
            entityId
        } else {
            message.manager.getGuild(entityId)?.let {
                if (it.isMember(message.getAuthor())) {
                    entityId
                } else {
                    null
                }
            }
        }
    }
    val template = entityId?.let { TemplateRepository.read(templateName, it) }
    return template?.let { TemplateCommand(it) }
}

private data class CommandConfig(
    val command: Command,
    val arguments: CommandArguments,
)

internal data class RawCommandConfig(
    val command: String,
    val arguments: Map<String, String>,
    val defaultArgument: String,
)

private class CommandNotFoundException : Exception()

private class NonChainableCommandException(
    val command1: Command,
    val command2: Command,
    cause: Throwable,
) : Exception(cause)

private class InsufficientPermissionsException(
    val command: Command,
    val requiredPermissions: Iterable<Permission>,
) : Exception()
