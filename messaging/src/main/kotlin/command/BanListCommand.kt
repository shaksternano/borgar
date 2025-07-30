package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.data.repository.BanRepository
import com.shakster.borgar.core.data.repository.EntityType
import com.shakster.borgar.core.util.asSingletonList
import com.shakster.borgar.core.util.splitChunks
import com.shakster.borgar.messaging.event.CommandEvent

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
