package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.exception.ErrorResponseException
import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.UrlFileTask
import com.shakster.borgar.core.util.ChannelEnvironment
import com.shakster.borgar.messaging.event.CommandEvent

object GuildBannerCommand : FileCommand(
    inputRequirement = InputRequirement.NONE,
) {

    override val name: String = "serverbanner"
    override val description: String = "Gets the banner image of this server."
    override val environment: Set<ChannelEnvironment> = setOf(ChannelEnvironment.GUILD)

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val guild = event.getGuild() ?: throw IllegalStateException("Command run outside of a guild")
        val bannerUrl = guild.bannerUrl ?: throw ErrorResponseException("This server has no banner image.")
        return UrlFileTask(bannerUrl)
    }
}
