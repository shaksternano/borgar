package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

object UserBannerCommand : FileCommand(
    CommandArgumentInfo(
        key = "user",
        description = "The user to get the banner from.",
        type = CommandArgumentType.User,
        required = false,
    ),
    inputRequirement = InputRequirement.NONE,
) {

    override val name: String = "userbanner"
    override val aliases: Set<String> = setOf("banner")
    override val description: String = "Gets the banner of a user."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val user = getReferencedUser(arguments, event)
        val bannerUrl = user.getBannerUrl() ?: throw ErrorResponseException("**${user.effectiveName}** has no banner.")
        return UrlFileTask(bannerUrl)
    }
}
