package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.data.repository.BanRepository
import io.github.shaksternano.borgar.core.data.repository.EntityType
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.messaging.event.CommandEvent

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
