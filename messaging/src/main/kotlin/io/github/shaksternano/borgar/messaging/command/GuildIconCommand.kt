package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask
import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.messaging.event.CommandEvent

object GuildIconCommand : FileCommand(
    inputRequirement = InputRequirement.None,
) {

    override val name: String = "servericon"
    override val aliases: Set<String> = setOf("icon")
    override val description: String = "Gets the icon of this server."
    override val environment: Set<ChannelEnvironment> = setOf(
        ChannelEnvironment.GUILD,
        ChannelEnvironment.GROUP,
    )

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val chatroom = event.getGuild()
            ?: event.getGroup()
            ?: throw IllegalStateException("Command run outside of a guild")
        val iconUrl = chatroom.iconUrl ?: throw ErrorResponseException("This server has no icon.")
        return UrlFileTask(iconUrl)
    }
}
