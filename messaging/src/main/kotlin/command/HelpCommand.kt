package com.shakster.borgar.messaging.command

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.shakster.borgar.core.BotConfig
import com.shakster.borgar.core.collect.getOrPut
import com.shakster.borgar.core.data.repository.TemplateRepository
import com.shakster.borgar.core.logger
import com.shakster.borgar.core.util.ChannelEnvironment
import com.shakster.borgar.core.util.Identified
import com.shakster.borgar.core.util.formatted
import com.shakster.borgar.core.util.splitChunks
import com.shakster.borgar.messaging.BotManager
import com.shakster.borgar.messaging.event.CommandEvent
import com.shakster.borgar.messaging.util.checkEntityIdBelongs
import com.shakster.borgar.messaging.util.getEntityId

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
    override val ephemeralReply: Boolean = true

    private val cachedCommandInfos: Cache<CommandInfoCacheKey, String> = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .build()

    override suspend fun run(
        arguments: CommandArguments,
        event: CommandEvent
    ): List<CommandResponse> {
        val entityId = event.getEntityId()
        val commandPrefix = BotConfig.get().commandPrefix
        val commandName = arguments.getDefaultStringOrEmpty()
            .removePrefix(commandPrefix)
        return if (commandName.isBlank()) {
            val environment = event.getEnvironment()
            getHelpMessages(
                entityId,
                environment,
                event.manager.maxMessageContentLength,
                event,
            ).map {
                CommandResponse(it, suppressEmbeds = true)
            }
        } else {
            val detailedCommandMessage = getDetailedCommandMessage(
                commandName,
                entityId,
                event,
            )
            detailedCommandMessage.splitChunks(event.manager.maxMessageContentLength).map {
                CommandResponse(it, suppressEmbeds = true)
            }
        }
    }

    fun removeCachedMessage(entityId: String) {
        cachedCommandInfos.invalidate(
            CommandInfoCacheKey(
                entityId = entityId,
                isOwner = false,
            ),
        )
        cachedCommandInfos.invalidate(
            CommandInfoCacheKey(
                entityId = entityId,
                isOwner = true,
            ),
        )
    }

    private suspend fun getHelpMessages(
        entityId: String,
        environment: ChannelEnvironment,
        maxContentLength: Int,
        event: CommandEvent,
    ): List<String> {
        val key = CommandInfoCacheKey(
            entityId = entityId,
            isOwner = event.authorId == event.manager.ownerId,
        )
        return cachedCommandInfos.getOrPut(key) {
            val commandInfos = getCommandInfo(entityId, environment, event)
            createHelpMessage(commandInfos)
        }.splitChunks(maxContentLength)
    }

    private fun createHelpMessage(commandInfo: Iterable<CommandInfo>): String {
        val commandDescriptions = commandInfo.sorted()
            .joinToString(separator = "\n") {
                "**" + it.name + "** - " + it.description
            }
        return "Commands:" +
            "\n$commandDescriptions\n\n" +
            "Use **$nameWithPrefix [command]** to get detailed information about a command."
    }

    private suspend fun getCommandInfo(
        entityId: String,
        environment: ChannelEnvironment,
        event: CommandEvent,
    ): List<CommandInfo> = buildList {
        COMMANDS.values.forEach {
            if (it.ownerOnly && event.authorId != event.manager.ownerId) {
                return@forEach
            }
            if (!it.isCorrectEnvironment(environment)) {
                return@forEach
            }
            add(CommandInfo(it.nameWithPrefix, it.description))
        }
        if (!TemplateRepository.connected) return@buildList
        val templates = try {
            TemplateRepository.readAll(entityId)
        } catch (t: Throwable) {
            logger.error("Failed to read templates", t)
            emptyList()
        }
        val commandPrefix = BotConfig.get().commandPrefix
        templates.forEach {
            add(CommandInfo(commandPrefix + it.commandName, it.description))
        }
    }

    private suspend fun getDetailedCommandMessage(
        commandName: String,
        entityId: String,
        event: CommandEvent,
    ): String {
        val manager = event.manager
        val command = COMMANDS_AND_ALIASES[commandName] ?: run {
            val entityIdSplit = commandName.split(ENTITY_ID_SEPARATOR, limit = 2)
            val (newCommandName, newEntityId) = if (entityIdSplit.size == 1) {
                commandName to entityId
            } else {
                entityIdSplit[0] to entityIdSplit[1]
            }
            if (!TemplateRepository.connected) return@run null
            val template = runCatching {
                TemplateRepository.read(newCommandName, newEntityId)
            }.getOrNull() ?: return@run null
            checkEntityIdBelongs(
                currentEnvironmentEntityId = entityId,
                toCheckEntityId = newEntityId,
                targetEnvironment = template.entityEnvironment,
                authorId = event.authorId,
                manager = manager,
            ) {
                return@run null
            }
            TemplateCommand(template)
        }
        if (command == null || (command.ownerOnly && event.authorId != manager.ownerId)) {
            return "Command **$commandName** not found!"
        }
        return "**${command.nameWithPrefix}** - ${command.description}\n" +
            getCommandAliasesMessage(command) +
            getArgumentsMessage(command) +
            getPermissionsMessage(command, manager) +
            getExtraInfoMessage(command, manager)
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

            message += "\n        Description: ${it.description}"
            message += "\n        Type: ${it.type.name}"

            val type = it.type
            if (type is CommandArgumentType.Enum<*>) {
                message += "\n        Possible values:"
                type.values.forEach { value ->
                    value as Identified
                    message += "\n            ${value.id}"
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
        manager: BotManager,
    ): String {
        var extraInfo = ""
        if (command.guildOnly) {
            extraInfo += "\n    Server only"
        }
        val entityId = command.entityId
        val entityEnvironment = command.entityEnvironment
        if (entityId != null && entityEnvironment != null) {
            when (entityEnvironment) {
                ChannelEnvironment.GUILD ->
                    manager.getGuild(entityId)?.let { guild ->
                        val name = guild.name
                        if (name != null) {
                            extraInfo += "\n    From the $name server"
                        }
                    }

                ChannelEnvironment.DIRECT_MESSAGE ->
                    extraInfo += "\n    Personal command"

                ChannelEnvironment.PRIVATE ->
                    extraInfo += "\n    Personal command"

                ChannelEnvironment.GROUP ->
                    manager.getGroup(entityId)?.let { group ->
                        val name = group.name
                        if (name != null) {
                            extraInfo += "\n    From the $name group"
                        }
                    }
            }
        }
        if (extraInfo.isNotBlank()) {
            extraInfo = "\nExtra info:$extraInfo"
        }
        return extraInfo
    }

    private data class CommandInfoCacheKey(
        val entityId: String,
        val isOwner: Boolean,
    )

    private data class CommandInfo(
        val name: String,
        val description: String,
    ) : Comparable<CommandInfo> {
        override fun compareTo(other: CommandInfo): Int =
            name.compareTo(other.name)
    }
}
