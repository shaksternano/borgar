package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.data.repository.BanRepository
import io.github.shaksternano.borgar.core.data.repository.EntityType
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.splitChunks
import io.github.shaksternano.borgar.messaging.event.CommandEvent

object BanListCommand : OwnerCommand() {

    override val name: String = "banlist"
    override val aliases: Set<String> = setOf("bans")
    override val description: String = "Lists all banned users from using this bot." +
        " Only the bot owner can use this command."
    override val ephemeralReply: Boolean = true

    override suspend fun runAsOwner(
        arguments: CommandArguments,
        event: CommandEvent,
    ): List<CommandResponse> {
        val bannedUsers = BanRepository.readAll(EntityType.USER, event.manager.platform)
        if (bannedUsers.isEmpty()) {
            return CommandResponse("No users are banned from using this bot.").asSingletonList()
        }
        val userMentions = bannedUsers.joinToString("\n") { id ->
            "${event.manager.formatUserMention(id)} ($id)"
        }
        return "Banned users:\n$userMentions"
            .splitChunks(event.manager.maxMessageContentLength)
            .map {
                CommandResponse(it)
            }
    }
}
