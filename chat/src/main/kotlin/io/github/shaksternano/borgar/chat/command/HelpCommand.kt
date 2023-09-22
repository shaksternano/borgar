package io.github.shaksternano.borgar.chat.command

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.data.repository.TemplateRepository
import io.github.shaksternano.borgar.core.util.splitChunks

object HelpCommand : NonChainableCommand() {

    override val name: String = "help"
    override val description: String = "Lists all commands."

    private val cachedCommandInfos: Cache<String, String> = CacheBuilder.newBuilder()
        .maximumSize(100)
        .build()

    override suspend fun runDirect(
        arguments: CommandArguments,
        event: CommandEvent
    ): List<CommandResponse> {
        val entityId = event.getGuild()?.id ?: event.getUser().id
        return getHelpMessages(entityId, event.manager.maxMessageContentLength).map(::CommandResponse)
    }

    suspend fun getHelpMessages(entityId: String, maxContentLength: Int): List<String> {
        val cached = cachedCommandInfos.getIfPresent(entityId)
        if (cached != null) {
            return cached.splitChunks(maxContentLength)
        }
        val commandInfos = getCommandInfo(entityId)
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

    private suspend fun getCommandInfo(entityId: String): List<CommandInfo> = buildList {
        COMMANDS.values.forEach {
            add(CommandInfo(it.nameWithPrefix, it.description))
        }
        val templates = TemplateRepository.readAll(entityId)
        templates.forEach {
            add(CommandInfo(COMMAND_PREFIX + it.commandName, it.description))
        }
    }

    private data class CommandInfo(val name: String, val description: String) : Comparable<CommandInfo> {
        override fun compareTo(other: CommandInfo): Int {
            return name.compareTo(other.name)
        }
    }
}
