package io.github.shaksternano.borgar.chat.command

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.data.repository.TemplateRepository
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.Displayed
import io.github.shaksternano.borgar.core.util.formatted
import io.github.shaksternano.borgar.core.util.splitChunks

object HelpCommand : NonChainableCommand() {

    override val name: String = "help"
    override val description: String = "Lists all commands."

    override val argumentInfo: Set<CommandArgumentInfo<*>> = setOf(
        CommandArgumentInfo(
            key = "command",
            description = "Get detailed information about a command.",
            type = CommandArgumentType.String,
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
        val commandName = arguments.getDefaultStringOrEmpty()
            .removePrefix(COMMAND_PREFIX)
            .lowercase()
        return if (commandName.isBlank()) {
            getHelpMessages(
                entityId,
                event.manager.maxMessageContentLength,
                fromGuild = guild != null
            ).map {
                CommandResponse(it, suppressEmbeds = true)
            }
        } else {
            val detailedCommandMessage = getDetailedCommandMessage(commandName, entityId, guild != null, event)
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
        event: CommandEvent
    ): String {
        val manager = event.manager
        val command = COMMANDS_AND_ALIASES[commandName] ?: run {
            val entityIdSplit = commandName.split(ENTITY_ID_SEPARATOR, limit = 2)
            val externalGuild = entityIdSplit.size == 2
            val (newCommandName, newEntityId) = if (externalGuild) {
                entityIdSplit[0] to entityIdSplit[1]
            } else {
                commandName to entityId
            }
            if (externalGuild && fromGuild) {
                val guild = manager.getGuild(newEntityId)
                if (guild == null || !guild.isMember(event.getAuthor())) {
                    return@run null
                }
            }
            runCatching { TemplateRepository.read(newCommandName, newEntityId) }.getOrNull()
                ?.let { TemplateCommand(it) }
        } ?: return "Command **$commandName** not found!"
        return "**${command.nameWithPrefix}** - ${command.description}\n" +
            getCommandAliasesMessage(command) +
            getArgumentsMessage(command) +
            getPermissionsMessage(command, manager) +
            getExtraInfoMessage(command, fromGuild, manager)
    }

    private fun getCommandAliasesMessage(command: Command): String {
        if (command.aliases.isEmpty()) return ""
        var message = "\nAliases:"
        command.aliases.forEach {
            message += "\n    **${it}**"
        }
        return message
    }

    private fun getArgumentsMessage(command: Command): String {
        if (command.argumentInfo.isEmpty()) return ""
        var message = "\nArguments:"
        command.argumentInfo.forEach {
            message += "\n    **${it.keyWithPrefix}**"
            var extraInfo = ""
            if (!it.required) {
                extraInfo += "optional"
            }
            if (it.key == command.defaultArgumentKey) {
                if (extraInfo.isNotBlank()) {
                    extraInfo += ", "
                }
                extraInfo += "default"
            }
            if (extraInfo.isNotBlank()) {
                message += " ($extraInfo)"
            }

            message += ":"
            if (it.aliases.isNotEmpty()) {
                message += "\n        Aliases:"
                it.aliases.forEach { alias ->
                    message += "\n            **$ARGUMENT_PREFIX${alias}**"
                }
            }

            message +=
                "\n        Description: ${it.description}" +
                    "\n        Type: ${it.type.name}"

            val type = it.type
            if (type is CommandArgumentType.Enum<*>) {
                message += "\n        Possible values:"
                type.values.forEach { value ->
                    value as Displayed
                    message += "\n            ${value.displayName}"
                }
            }

            it.defaultValue?.let { defaultValue ->
                message += "\n        Default value: ${defaultValue.formatted}"
            }

            if (it.validator.description.isNotBlank()) {
                message += "\n        Constraints: ${it.validator.description}"
            }
        }
        return message
    }

    private fun getPermissionsMessage(command: Command, manager: BotManager): String {
        if (command.requiredPermissions.isEmpty()) return ""
        var message = "\nRequired permissions:"
        command.requiredPermissions.forEach {
            message += "\n    ${manager.getPermissionName(it)}"
        }
        return message
    }

    private suspend fun getExtraInfoMessage(
        command: Command,
        fromGuild: Boolean,
        manager: BotManager
    ): String {
        var extraInfo = ""
        if (command.guildOnly) {
            extraInfo += "\n    Server only"
        }
        command.entityId?.let {
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
