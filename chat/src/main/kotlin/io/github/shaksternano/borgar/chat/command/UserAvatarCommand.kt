package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask
import kotlinx.coroutines.flow.firstOrNull

object UserAvatarCommand : FileCommand(
    CommandArgumentInfo(
        key = "user",
        description = "The user to get the avatar from.",
        type = CommandArgumentType.User,
        required = false,
    ),
    CommandArgumentInfo(
        key = "server",
        description = "Wether to get the server profile avatar or not.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = true,
    ),
    requireInput = false,
) {

    override val name: String = "avatar"
    override val description: String = "Gets the avatar of a user."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val argumentUser = arguments.getOptional("user", CommandArgumentType.User)
        val guildAvatar = arguments.getRequired("server", CommandArgumentType.Boolean)
        val user = argumentUser ?: run {
            val messageIntersection = event.asMessageIntersection(arguments)
            val referencedMessage = messageIntersection.referencedMessages.firstOrNull()
            val referencedUser = referencedMessage?.getAuthor()
            referencedUser ?: event.getAuthor()
        }
        val avatarUrl = run {
            if (guildAvatar) {
                val member = event.getGuild()?.getMember(user.id)
                if (member != null) {
                    return@run member.effectiveAvatarUrl
                }
            }
            user.effectiveAvatarUrl
        } + "?size=1024"
        return UrlFileTask(avatarUrl)
    }
}
