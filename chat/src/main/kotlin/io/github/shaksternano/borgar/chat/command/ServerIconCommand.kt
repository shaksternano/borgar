package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.exception.FailedOperationException
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask

object ServerIconCommand : FileCommand(
    inputRequirement = InputRequirement.None,
) {

    override val name: String = "servericon"
    override val aliases: Set<String> = setOf("icon")
    override val description: String = "Gets the icon of this server."
    override val guildOnly: Boolean = true

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val guild = event.getGuild() ?: throw IllegalStateException("Command run outside of a guild")
        val iconUrl = guild.iconUrl ?: throw FailedOperationException("This server has no icon.")
        return UrlFileTask(iconUrl)
    }
}
