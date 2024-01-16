package io.github.shaksternano.borgar.chat.command

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.data.repository.TemplateRepository
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.formatted
import io.github.shaksternano.borgar.core.util.splitChunks

object HelpCommand : NonChainableCommand() {

    override val name: String = "help"
    override val description: String = "Lists all commands."

    override val argumentInfo: Set<CommandArgumentInfo<*>> = setOf(
        CommandArgumentInfo(
            key = "command",
            description = "Get detailed information about a command.",
            type = SimpleCommandArgumentType.STRING,
            required = false,
        )
    )

    private val cachedCommandInfos: Cache<String, String> = CacheBuilder.newBuilder()
        .maximumSize(100)
        .build()

    override suspend fun runDirect(
        arguments: CommandArguments,
        event: CommandEvent
    ): List<CommandResponse> {
        val guild = event.getGuild()
        val entityId = guild?.id ?: event.getAuthor().id
        val commandName = arguments.getDefaultStringOrEmpty().lowercase()
        return if (commandName.isBlank()) {
            getHelpMessages(entityId, event.manager.maxMessageContentLength, guild != null).map {
                CommandResponse(it, suppressEmbeds = true)
            }
        } else {
            val detailedCommandMessage = getDetailedCommandMessage(commandName, entityId, guild != null, event.manager)
            detailedCommandMessage.splitChunks(event.manager.maxMessageContentLength).map {
                CommandResponse(it, suppressEmbeds = true)
            }
        }
    }

    private suspend fun getHelpMessages(entityId: String, maxContentLength: Int, fromGuild: Boolean): List<String> {
        val cached = cachedCommandInfos.getIfPresent(entityId)
        if (cached != null) {
            return cached.splitChunks(maxContentLength)
        }
        val commandInfos = getCommandInfo(entityId, fromGuild)
        val helpMessage = createHelpMessage(commandInfos)
        cachedCommandInfos.put(entityId, helpMessage)
        return helpMessage.splitChunks(maxContentLength)
    }

    private fun createHelpMessage(commandInfo: Iterable<CommandInfo>): String {
        val commandDescriptions = commandInfo.sorted()
            .joinToString(separator = "") {
                "**" + it.name + "** - " + it.description + "\n"
            }
        return "Commands:\n\n$commandDescriptions"
    }

    private suspend fun getCommandInfo(entityId: String, fromGuild: Boolean): List<CommandInfo> = buildList {
        COMMANDS.values.forEach {
            if (fromGuild || !it.guildOnly) {
                add(CommandInfo(it.nameWithPrefix, it.description))
            }
        }
        val templates = try {
            TemplateRepository.readAll(entityId)
        } catch (t: Throwable) {
            logger.error("Failed to read templates", t)
            emptyList()
        }
        templates.forEach {
            add(CommandInfo(COMMAND_PREFIX + it.commandName, it.description))
        }
    }

    private suspend fun getDetailedCommandMessage(
        commandName: String,
        entityId: String,
        fromGuild: Boolean,
        manager: BotManager
    ): String {
        val command = COMMANDS[commandName] ?: run {
            val entityIdSplit = commandName.split(ENTITY_ID_SEPARATOR, limit = 2)
            val (newCommandName, newEntityId) = if (entityIdSplit.size == 2) {
                entityIdSplit[0] to entityIdSplit[1]
            } else {
                commandName to entityId
            }
            runCatching { TemplateRepository.read(newCommandName, newEntityId) }.getOrNull()?.let { TemplateCommand(it) }
        } ?: return "Command `$commandName` not found!"
        val argumentInfo = command.argumentInfo
        val defaultArgumentKey = command.defaultArgumentKey
        val guildOnly = command.guildOnly
        val requiredPermissions = command.requiredPermissions
        val commandEntityId = command.entityId
        var argumentsMessage = "`${command.name}` - ${command.description}"
        argumentsMessage += "\n\nArguments:\n${getArgumentsMessage(argumentInfo, defaultArgumentKey)}"
        if (requiredPermissions.isNotEmpty()) {
            argumentsMessage += "\nRequired permissions:\n${getPermissionsMessage(requiredPermissions, manager)}"
        }
        argumentsMessage += getExtraInfoMessage(commandEntityId, guildOnly, fromGuild, manager)
        return argumentsMessage
    }

    private fun getArgumentsMessage(
        argumentInfo: Iterable<CommandArgumentInfo<*>>,
        defaultArgumentKey: String?
    ): String =
        argumentInfo.joinToString(separator = "\n") {
            var argumentMessage = "    `${it.key}`"
            var extraInfo = ""
            if (!it.required) {
                extraInfo += "optional"
            }
            if (it.key == defaultArgumentKey) {
                if (extraInfo.isNotBlank()) {
                    extraInfo += ", "
                }
                extraInfo += "default"
            }

            if (extraInfo.isNotBlank()) {
                argumentMessage += " ($extraInfo)"
            }
            argumentMessage += ":"
            argumentMessage += "\n        Description: ${it.description}"
            argumentMessage += "\n        Type: ${it.type.name}"
            it.defaultValue?.let { defaultValue ->
                argumentMessage += "\n        Default value: ${defaultValue.formatted}"
            }
            argumentMessage
        }

    private fun getPermissionsMessage(requiredPermissions: Iterable<Permission>, manager: BotManager): String =
        requiredPermissions.joinToString(separator = "\n") {
            "    `${manager.getPermissionName(it)}`"
        }

    private suspend fun getExtraInfoMessage(
        entityId: String?,
        guildOnly: Boolean,
        fromGuild: Boolean,
        manager: BotManager
    ): String {
        var extraInfo = ""
        if (guildOnly) {
            extraInfo += "\n    Server only"
        }
        entityId?.let {
            if (fromGuild) {
                manager.getGuild(it)?.let { guild ->
                    extraInfo += "\n    From the ${guild.name} server"
                }
            } else {
                extraInfo += "\n    Personal command"
            }
        }
        if (extraInfo.isNotBlank()) {
            extraInfo = "\nExtra info:$extraInfo"
        }
        return extraInfo
    }

    private data class CommandInfo(
        val name: String,
        val description: String,
    ) : Comparable<CommandInfo> {
        override fun compareTo(other: CommandInfo): Int =
            name.compareTo(other.name)
    }
}
