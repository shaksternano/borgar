package com.shakster.borgar.discord.interaction.modal

import com.shakster.borgar.core.BotConfig
import com.shakster.borgar.core.logger
import com.shakster.borgar.core.util.MessagingPlatform
import com.shakster.borgar.discord.entity.DiscordUser
import com.shakster.borgar.discord.entity.channel.DiscordMessageChannel
import com.shakster.borgar.discord.event.DiscordInteractionCommandEvent
import com.shakster.borgar.messaging.command.CommandNotFoundException
import com.shakster.borgar.messaging.command.isCorrectEnvironment
import com.shakster.borgar.messaging.command.parseCommands
import com.shakster.borgar.messaging.entity.FakeMessage
import com.shakster.borgar.messaging.executeAndRespond
import com.shakster.borgar.messaging.util.getAndExpireSelectedMessage
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

object RunCommandInteractionCommand : DiscordModalInteractionCommand {

    override val name: String = "command"
    const val TEXT_INPUT_ID: String = "command"

    override suspend fun respond(event: ModalInteractionEvent): Any? {
        val commandPrefix = BotConfig.get().commandPrefix
        val content = event.getValue(TEXT_INPUT_ID)
            ?.asString
            ?.trim()
            ?.let {
                if (it.startsWith(commandPrefix)) {
                    it
                } else {
                    "$commandPrefix$it"
                }
            }
            ?: run {
                logger.error("Command value is null")
                event.reply("An error occurred!")
                    .setEphemeral(true)
                    .await()
                return null
            }
        runCatching {
            val discordChannel = event.channel
            val channel = DiscordMessageChannel(discordChannel, event.context)
            val message = FakeMessage(
                id = event.id,
                content = content,
                author = DiscordUser(event.user),
                channel = channel,
            )

            val commandConfigs = try {
                parseCommands(content, message, event.user.id)
            } catch (e: CommandNotFoundException) {
                event.reply("The command **$commandPrefix${e.command}** does not exist!")
                    .setEphemeral(true)
                    .await()
                return null
            }

            if (commandConfigs.isEmpty()) {
                event.reply("No commands found!")
                    .setEphemeral(true)
                    .await()
                return null
            }

            val environment = channel.environment
            val firstCommand = commandConfigs.first().command
            if (!firstCommand.isCorrectEnvironment(environment)) {
                event.reply("The command **$commandPrefix${firstCommand.name}** cannot be used in a ${environment.displayName.lowercase()} channel!")
                    .setEphemeral(true)
                    .await()
                return null
            }

            // Message interaction event message is not preserved so modal event message is null
            val referencedMessage = getAndExpireSelectedMessage(
                userId = event.user.id,
                channelId = discordChannel.id,
                platform = MessagingPlatform.DISCORD,
            ) ?: run {
                logger.error("Command modal referenced message is null")
                event.reply("An error occurred!")
                    .setEphemeral(true)
                    .await()
                return null
            }
            val commandEvent = DiscordInteractionCommandEvent(
                event,
                discordChannel,
                referencedMessage = referencedMessage,
            )
            commandEvent.executeAndRespond(commandConfigs)
        }.getOrElse {
            logger.error("Error handling modal command $content", it)
            event.reply("An error occurred!")
                .setEphemeral(true)
                .await()
        }
        return null
    }
}
