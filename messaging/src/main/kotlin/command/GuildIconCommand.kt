package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.exception.ErrorResponseException
import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.UrlFileTask
import com.shakster.borgar.core.util.ChannelEnvironment
import com.shakster.borgar.messaging.event.CommandEvent

object GuildIconCommand : FileCommand(
    inputRequirement = InputRequirement.NONE,
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
