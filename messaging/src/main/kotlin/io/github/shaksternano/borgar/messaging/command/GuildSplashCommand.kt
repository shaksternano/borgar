package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask
import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.messaging.event.CommandEvent

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
