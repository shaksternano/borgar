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
import io.github.shaksternano.borgar.core.data.repository.TemplateRepository
import io.github.shaksternano.borgar.core.exception.FailedOperationException
import io.github.shaksternano.borgar.core.logger
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
    val commandEvent = MessageCommandEvent(event)
    val (responses, executable) = sendTypingUntilDone(event.getChannel()) {
        executeCommands(commandConfigs, commandEvent)
    }
    responses.send(executable, commandEvent)
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
    }
}

suspend inline fun executeCommands(
    commandConfigs: List<CommandConfig>,
    event: CommandEvent,
    firstCommandError: () -> Unit = {},
): Pair<List<CommandResponse>, Executable?> = try {
    val executables = commandConfigs.mapIndexed { i, (command, arguments) ->
        val guild = event.getGuild()
        if (guild == null && command.guildOnly) {
            if (i == 0) firstCommandError()
            throw GuildOnlyCommandException(command)
        }
        if (guild != null) {
            val requiredPermissions = command.requiredPermissions
            val permissionHolder = guild.getMember(event.getAuthor()) ?: guild.getPublicRole()
            val hasPermission = permissionHolder.hasPermission(requiredPermissions, event.getChannel())
            if (!hasPermission) {
                if (i == 0) firstCommandError()
                throw InsufficientPermissionsException(command, requiredPermissions)
            }
        }

        try {
            command.run(arguments, event)
        } catch (t: Throwable) {
            throw CommandException(listOf(command), cause = t)
        }
    }
    val chained = executables.reduce { executable1, executable2 ->
        try {
            executable1 then executable2
        } catch (e: UnsupportedOperationException) {
            throw NonChainableCommandException(
                executable1.commands.last(),
                executable2.commands.first(),
                e,
            )
        }
    }
    val result = try {
        chained.execute()
    } catch (t: Throwable) {
        throw CommandException(chained.commands, cause = t)
    }
    if (result.isEmpty()) {
        throw CommandException(chained.commands, "No command responses were returned")
    }
    result.forEach {
        if (it.content.isBlank() && it.files.isEmpty()) {
            throw CommandException(chained.commands, "Command response is empty")
        }
    }
    result to chained
} catch (t: Throwable) {
    val responseContent = handleError(t, event.manager)
    listOf(CommandResponse(responseContent)) to null
}

suspend fun List<CommandResponse>.send(
    executable: Executable?,
    commandEvent: CommandEvent,
) = coroutineScope {
    var sendHandleResponseErrorMessage = true
    forEachIndexed { index, response ->
        try {
            val sent = commandEvent.reply(response)
            launch {
                try {
                    executable?.onResponseSend(
                        response,
                        index + 1,
                        size,
                        sent,
                        commandEvent
                    )
                } catch (t: Throwable) {
                    logger.error("An error occurred", t)
                    if (sendHandleResponseErrorMessage) {
                        commandEvent.reply("An error occurred!")
                        sendHandleResponseErrorMessage = false
                    }
                }
            }
        } catch (t: Throwable) {
            logger.error("Failed to send response", t)
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

fun handleError(throwable: Throwable, manager: BotManager): String = when (throwable) {
    is NonChainableCommandException ->
        "Cannot chain **${throwable.command1.nameWithPrefix}** with **${throwable.command2.nameWithPrefix}**!"

    is CommandException -> when (val cause = throwable.cause) {
        is FailedOperationException ->
            cause.message

        is MissingArgumentException ->
            cause.message

        else -> {
            logger.error(
                "Error executing commands ${
                    throwable.commands.joinToString(", ") {
                        it.name
                    }
                }", throwable
            )
            "An error occurred!"
        }
    }

    is InsufficientPermissionsException ->
        "**${throwable.command.nameWithPrefix}** requires permissions:\n${
            throwable.requiredPermissions.joinToString(
                "\n"
            ) { manager.getPermissionName(it) }
        }!"

    is GuildOnlyCommandException ->
        "**${throwable.command.nameWithPrefix}** can only be used in a guild!"

    else -> {
        logger.error("An error occurred", throwable)
        "An error occurred!"
    }
}

private suspend fun userDetails(user: User, guild: Guild?): DisplayedUser {
    return guild?.getMember(user)?.user ?: user
}

suspend fun parseCommands(messageContent: String, message: Message): List<CommandConfig> {
    return parseRawCommands(messageContent)
        .mapIndexed { index, (commandString, rawArguments, defaultArgumentValue) ->
            val command = COMMANDS_AND_ALIASES[commandString] ?: getCustomTemplateCommand(
                commandString,
                message
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
                message
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
    val template = entityId?.runCatching {
        TemplateRepository.read(templateName, this)
    }?.getOrNull()
    return template?.let { TemplateCommand(it) }
}

data class CommandConfig(
    val command: Command,
    val arguments: CommandArguments,
)

internal data class RawCommandConfig(
    val command: String,
    val arguments: Map<String, String>,
    val defaultArgument: String,
)

class CommandNotFoundException(
    val command: String,
) : Exception()

class NonChainableCommandException(
    val command1: Command,
    val command2: Command,
    override val cause: Throwable,
) : Exception(cause)

class GuildOnlyCommandException(
    val command: Command,
) : Exception()

class InsufficientPermissionsException(
    val command: Command,
    val requiredPermissions: Iterable<Permission>,
) : Exception()
