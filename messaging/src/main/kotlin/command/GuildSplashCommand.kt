package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.exception.ErrorResponseException
import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.UrlFileTask
import com.shakster.borgar.core.util.ChannelEnvironment
import com.shakster.borgar.messaging.event.CommandEvent

object GuildSplashCommand : FileCommand(
    inputRequirement = InputRequirement.NONE,
) {

    override val name: String = "serversplash"
    override val aliases: Set<String> = setOf("splash")
    override val description: String = "Gets the splash image of this server."
    override val environment: Set<ChannelEnvironment> = setOf(ChannelEnvironment.GUILD)

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val guild = event.getGuild() ?: throw IllegalStateException("Command run outside of a guild")
        val splashUrl = guild.splashUrl ?: throw ErrorResponseException("This server has no splash image.")
        return UrlFileTask(splashUrl)
    }
}
