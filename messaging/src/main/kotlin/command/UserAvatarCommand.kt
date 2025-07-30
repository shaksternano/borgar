package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.UrlFileTask
import com.shakster.borgar.messaging.event.CommandEvent

object UserAvatarCommand : FileCommand(
    CommandArgumentInfo(
        key = "user",
        description = "The user to get the avatar from.",
        type = CommandArgumentType.User,
        required = false,
    ),
    CommandArgumentInfo(
        key = "noserver",
        description = "Whether to ignore the server avatar or not.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
    inputRequirement = InputRequirement.NONE,
) {

    override val name: String = "useravatar"
    override val aliases: Set<String> = setOf("avatar")
    override val description: String = "Gets the avatar of a user."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val user = getReferencedUser(arguments, event)
        val ignoreServer = arguments.getRequired("noserver", CommandArgumentType.Boolean)
        val avatarUrl = run {
            if (!ignoreServer) {
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
