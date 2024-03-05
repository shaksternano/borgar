package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.ChannelEnvironment
import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask

object GuildSplashCommand : FileCommand(
    inputRequirement = InputRequirement.None,
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
