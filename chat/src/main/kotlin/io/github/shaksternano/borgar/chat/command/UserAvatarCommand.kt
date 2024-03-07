package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask

object UserAvatarCommand : FileCommand(
    CommandArgumentInfo(
        key = "user",
        description = "The user to get the avatar from.",
        type = CommandArgumentType.User,
        required = false,
    ),
    CommandArgumentInfo(
        key = "server",
        aliases = setOf("s"),
        description = "Whether to get the avatar from the server profile or not.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
    inputRequirement = InputRequirement.None,
) {

    override val name: String = "useravatar"
    override val aliases: Set<String> = setOf("avatar")
    override val description: String = "Gets the avatar of a user."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val user = getReferencedUser(arguments, event)
        val guildAvatar = arguments.getRequired("server", CommandArgumentType.Boolean)
        val avatarUrl = run {
            if (guildAvatar) {
                val member = event.getGuild()?.getMember(user.id)
                if (member != null) {
                    return@run member.effectiveAvatarUrl
                }
            }
            user.effectiveAvatarUrl
        }
        return UrlFileTask(avatarUrl)
    }
}
