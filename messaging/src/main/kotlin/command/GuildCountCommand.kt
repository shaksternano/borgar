package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.util.MessagingPlatform
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.messaging.BOT_MANAGERS
import io.github.shaksternano.borgar.messaging.event.CommandEvent

private val MESSAGING_PLATFORM_TYPE: CommandArgumentType<MessagingPlatform> =
    CommandArgumentType.Enum<MessagingPlatform>("messaging platform")

object GuildCountCommand : NonChainableCommand() {

    override val name: String = "servercount"
    override val aliases: Set<String> = setOf("servers")
    override val description: String = "Gets the number of servers that this bot is in."
    override val argumentInfo: Set<CommandArgumentInfo<*>> = setOf(
        CommandArgumentInfo(
            key = "platform",
            description = "The messaging platform to get the server count for.",
            type = MESSAGING_PLATFORM_TYPE,
            required = false,
        ),
    )

    override suspend fun run(arguments: CommandArguments, event: CommandEvent): List<CommandResponse> {
        val platform = arguments.getOptional("platform", MESSAGING_PLATFORM_TYPE)
        val managers =
            if (platform == null) BOT_MANAGERS
            else BOT_MANAGERS.filter {
                it.platform == platform
            }
        val guildCount = managers.sumOf {
            it.getGuildCount()
        }
        var message = "This bot is in $guildCount "
        if (platform != null) {
            message += "${platform.displayName} "
        }
        message += "server"
        if (guildCount != 1) {
            message += "s"
        }
        message += "."
        return CommandResponse(message).asSingletonList()
    }
}
