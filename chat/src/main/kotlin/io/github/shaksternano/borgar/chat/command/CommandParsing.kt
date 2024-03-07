package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.DisplayedUser
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.event.MessageCommandEvent
import io.github.shaksternano.borgar.chat.event.MessageReceiveEvent
import io.github.shaksternano.borgar.chat.exception.CommandException
import io.github.shaksternano.borgar.chat.exception.MissingArgumentException
import io.github.shaksternano.borgar.chat.util.checkEntityIdBelongs
import io.github.shaksternano.borgar.chat.util.getEntityId
import io.github.shaksternano.borgar.core.collect.parallelForEach
import io.github.shaksternano.borgar.core.data.repository.TemplateRepository
import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.deleteSilently
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.core.util.endOfWord
import io.github.shaksternano.borgar.core.util.indicesOfPrefix
import io.github.shaksternano.borgar.core.util.split
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.launch

suspend fun parseAndExecuteCommand(event: MessageReceiveEvent) {
    val contentStripped = contentStripped(event.message).trim()
    if (!contentStripped.startsWith(COMMAND_PREFIX)) return
    val commandConfigs = try {
        parseCommands(contentStripped, event.message)
    } catch (e: CommandNotFoundException) {
        event.reply("The command **$COMMAND_PREFIX${e.command}** does not exist!")
        return
    }
    if (commandConfigs.isEmpty()) return
    val channel = event.getChannel()
    val environment = channel.environment
    val firstCommand = commandConfigs.first().command
    if (!firstCommand.isCorrectEnvironment(environment)) return
    val commandEvent = MessageCommandEvent(event)
    sendTypingUntilDone(channel) {
        val (responses, executable) = executeCommands(
            commandConfigs,
            environment,
            commandEvent,
        )
        sendResponses(responses, executable, commandEvent)
    }
}

private suspend fun <T> sendTypingUntilDone(
    channel: MessageChannel,
    block: suspend () -> T,
): T = coroutineScope {
    var sendTyping = true
    val typingDuration = channel.manager.typingDuration
    val typing = launch {
        while (sendTyping) {
            channel.sendTyping()
            delay(typingDuration)
        }
    }
    block().also {
        sendTyping = false
        typing.cancel()
        channel.stopTyping()
    }
}

suspend inline fun executeCommands(
    commandConfigs: List<CommandConfig>,
    environment: ChannelEnvironment,
    event: CommandEvent,
): Pair<List<CommandResponse>, Executable?> =
    runCatching {
        val executables = commandConfigs.map {
            val (command, arguments) = it
            val guild = event.getGuild()
            if (!command.isCorrectEnvironment(environment)) {
                throw IncorrectChannelEnvironmentException(command, environment)
            }
            if (guild != null) {
                val requiredPermissions = command.requiredPermissions
                val permissionHolder = guild.getMember(event.authorId) ?: guild.publicRole
                val hasPermission = permissionHolder.hasPermission(requiredPermissions, event.getChannel())
                if (!hasPermission) {
                    throw InsufficientPermissionsException(command, requiredPermissions)
                }
            }
            try {
                command.createExecutable(arguments, event)
            } catch (t: Throwable) {
                throw CommandException(listOf(it), cause = t)
            }
        }
        val chained = executables.reduce { executable1, executable2 ->
            executable1 then executable2
        }
        val result = try {
            chained.run()
        } catch (t: Throwable) {
            throw CommandException(chained.commandConfigs, cause = t)
        }
        if (result.isEmpty())
            throw CommandException(chained.commandConfigs, "No command responses were returned")
        result.forEach {
            if (it.content.isBlank() && it.files.isEmpty())
                throw CommandException(chained.commandConfigs, "Command response is empty")
        }
        result to chained
    }.getOrElse { t ->
        val responseContent = handleError(t, event.manager)
        listOf(CommandResponse(responseContent)) to null
    }

suspend fun sendResponses(
    responses: List<CommandResponse>,
    executable: Executable?,
    commandEvent: CommandEvent,
) {
    var sendHandleResponseErrorMessage = true
    responses.forEachIndexed { index, response ->
        try {
            val sent = commandEvent.reply(response)
            runCatching {
                executable?.onResponseSend(
                    response,
                    index + 1,
                    responses.size,
                    sent,
                    commandEvent,
                )
            }.onFailure { t ->
                logger.error("An error occurred", t)
                if (sendHandleResponseErrorMessage) {
                    commandEvent.reply("An error occurred!")
                    sendHandleResponseErrorMessage = false
                }
            }
        } catch (t: Throwable) {
            logger.error("Failed to send response", t)
            runCatching {
                commandEvent.reply("Error sending response!")
            }
        } finally {
            response.files.parallelForEach {
                it.path?.deleteSilently()
            }
        }
    }
    executable?.close()
}

private suspend fun contentStripped(message: Message): String {
    var stripped = message.content
    stripped = message.mentionedUsers.fold(stripped) { content, user ->
        val details = userDetails(user, message.getGuild())
        content.replace(
            "<@!?${user.id}>".toRegex(),
            "@${details.effectiveName}"
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

private const val OUT_OF_MEMORY_ERROR_MESSAGE = "Ran out of memory! Please try again later, or use a smaller file."

fun handleError(throwable: Throwable, manager: BotManager): String {
    val (unwrapped, commandConfigs) =
        if (throwable is CommandException)
            (throwable.cause ?: throwable) to throwable.commandConfigs
        else throwable to emptyList()
    return when (unwrapped) {
        is ErrorResponseException -> {
            unwrapped.cause?.let {
                logger.error("An error occurred", it)
            }
            unwrapped.message
        }

        is NonChainableCommandException -> unwrapped.message

        is MissingArgumentException -> unwrapped.message

        is InsufficientPermissionsException ->
            "**${unwrapped.command.nameWithPrefix}** requires permissions:\n${
                unwrapped.requiredPermissions.joinToString(
                    "\n"
                ) {
                    manager.getPermissionName(it)
                }
            }"

        is IncorrectChannelEnvironmentException ->
            "**${unwrapped.command.nameWithPrefix}** cannot be used in a ${unwrapped.environment.displayName}!"

        is OutOfMemoryError -> OUT_OF_MEMORY_ERROR_MESSAGE

        else -> {
            if (commandConfigs.isEmpty()) {
                logger.error("An error occurred", unwrapped)
            } else {
                var message = "Error executing command"
                if (commandConfigs.size > 1) {
                    message += "s"
                }
                message += " "
                message += commandConfigs.joinToString(", ") {
                    it.typedForm
                }
                logger.error(
                    message,
                    unwrapped,
                )
            }
            "An error occurred!"
        }
    }
}

private suspend fun userDetails(user: User, guild: Guild?): DisplayedUser =
    guild?.getMember(user) ?: user

suspend fun parseCommands(messageContent: String, message: Message): List<CommandConfig> {
    return parseRawCommands(messageContent)
        .mapIndexed { index, (commandString, rawArguments, defaultArgumentValue) ->
            val command = COMMANDS_AND_ALIASES[commandString] ?: getCustomTemplateCommand(
                commandString,
                message,
            )
            if (command == null) {
                if (index > 0) {
                    throw CommandNotFoundException(commandString)
                } else {
                    return emptyList()
                }
            }
            val arguments = MessageCommandArguments(
                rawArguments,
                command.defaultArgumentKey,
                defaultArgumentValue,
                command.argumentInfo,
                message,
            )
            CommandConfig(command, arguments)
        }
}

internal fun parseRawCommands(message: String): List<RawCommandConfig> {
    return parseCommandStrings(message).map { commandString ->
        val commandEndIndex = commandString.endOfWord(COMMAND_PREFIX.length)
        val command = commandString.substring(COMMAND_PREFIX.length, commandEndIndex)
        val argumentPrefixIndexes = commandString.indicesOfPrefix(ARGUMENT_PREFIX)
        val arguments = commandString.split(argumentPrefixIndexes)
            .associate {
                val argumentNameEndIndex = it.endOfWord(ARGUMENT_PREFIX.length)
                val argumentName = it.substring(ARGUMENT_PREFIX.length, argumentNameEndIndex)
                val argumentValue = it.substring(argumentNameEndIndex)
                    .trim()
                    .ifBlank { "true" } // Flag argument
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
    val commandPrefixIndexes = message.indicesOfPrefix(COMMAND_PREFIX)
    return message.split(commandPrefixIndexes).map { it.trim() }
}

private suspend fun getCustomTemplateCommand(commandName: String, message: Message): Command? {
    val commandNameParts = commandName.split(ENTITY_ID_SEPARATOR, limit = 2)
    val templateName = commandNameParts[0]
    val messageEntityId = message.getEntityId()
    val entityId = if (commandNameParts.size == 1) {
        messageEntityId
    } else {
        commandNameParts[1]
    }
    val template = runCatching {
        TemplateRepository.read(templateName, entityId)
    }.getOrNull() ?: return null
    checkEntityIdBelongs(
        currentEnvironmentEntityId = messageEntityId,
        toCheckEntityId = entityId,
        targetEnvironment = template.entityEnvironment,
        authorId = message.authorId,
        manager = message.manager,
    ) {
        return null
    }
    return TemplateCommand(template)
}

data class CommandConfig(
    val command: Command,
    val arguments: CommandArguments,
) {
    val typedForm: String = "$COMMAND_PREFIX${command.name}" + run {
        val argumentsTypedForm = arguments.typedForm
        if (argumentsTypedForm.isNotBlank()) " $argumentsTypedForm"
        else ""
    }
}

internal data class RawCommandConfig(
    val command: String,
    val arguments: Map<String, String>,
    val defaultArgument: String,
)

class CommandNotFoundException(
    val command: String,
) : Exception()

class NonChainableCommandException(
    commandConfig1: CommandConfig,
    commandConfig2: CommandConfig,
) : Exception() {
    override val message: String = "Cannot chain **${commandConfig1.typedForm}** with **${commandConfig2.typedForm}**!"
}

class IncorrectChannelEnvironmentException(
    val command: Command,
    val environment: ChannelEnvironment,
) : Exception()

class InsufficientPermissionsException(
    val command: Command,
    val requiredPermissions: Iterable<Permission>,
) : Exception()
