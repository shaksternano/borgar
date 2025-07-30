package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.data.repository.BanRepository
import com.shakster.borgar.core.data.repository.EntityType
import com.shakster.borgar.core.logger
import com.shakster.borgar.core.util.asSingletonList
import com.shakster.borgar.messaging.event.CommandEvent

object UnbanCommand : OwnerCommand() {

    override val name: String = "unban"
    override val description: String = "Unbans a user, channel, or guild from using this bot." +
        " Only the bot owner can use this command."
    override val ephemeralReply: Boolean = true

    override val argumentInfo: Set<CommandArgumentInfo<*>> = setOf(
        CommandArgumentInfo(
            key = "id",
            description = "The id of the user to unban.",
            type = CommandArgumentType.String,
        ),
    )

    override suspend fun runAsOwner(
        arguments: CommandArguments,
        event: CommandEvent,
    ): List<CommandResponse> {
        val id = arguments.getRequired("id", CommandArgumentType.String)
        val platform = event.manager.platform
        BanRepository.delete(id, EntityType.USER, platform)
        logger.info("Unbanned user $id on ${platform.displayName}")
        val userMention = event.manager.formatUserMention(id)
        return CommandResponse(
            "Unbanned $userMention ($id) from using this bot on ${platform.displayName}.",
        ).asSingletonList()
    }
}
