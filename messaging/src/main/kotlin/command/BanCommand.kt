package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.data.repository.BanRepository
import com.shakster.borgar.core.data.repository.EntityType
import com.shakster.borgar.core.logger
import com.shakster.borgar.core.util.asSingletonList
import com.shakster.borgar.messaging.event.CommandEvent

object BanCommand : OwnerCommand() {

    override val name: String = "ban"
    override val description: String = "Bans a user from using this bot." +
        " Only the bot owner can use this command."
    override val ephemeralReply: Boolean = true

    override val argumentInfo: Set<CommandArgumentInfo<*>> = setOf(
        CommandArgumentInfo(
            key = "id",
            description = "The id of the user to ban.",
            type = CommandArgumentType.String,
        ),
    )

    override suspend fun runAsOwner(
        arguments: CommandArguments,
        event: CommandEvent,
    ): List<CommandResponse> {
        val id = arguments.getRequired("id", CommandArgumentType.String)
        if (id == event.authorId) {
            return CommandResponse("You cannot ban yourself!").asSingletonList()
        }
        if (id == event.manager.selfId) {
            return CommandResponse("You cannot ban the bot itself!").asSingletonList()
        }
        val platform = event.manager.platform
        BanRepository.create(id, EntityType.USER, platform)
        logger.info("Banned user $id on ${platform.displayName}")
        val userMention = event.manager.formatUserMention(id)
        return CommandResponse(
            "Banned $userMention ($id) from using this bot on ${platform.displayName}.",
        ).asSingletonList()
    }
}
