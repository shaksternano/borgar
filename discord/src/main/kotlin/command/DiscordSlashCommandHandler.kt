package com.shakster.borgar.discord.command

import com.shakster.borgar.core.BotConfig
import com.shakster.borgar.core.data.repository.TemplateRepository
import com.shakster.borgar.core.logger
import com.shakster.borgar.core.util.asSingletonList
import com.shakster.borgar.core.util.splitWords
import com.shakster.borgar.discord.DiscordManager
import com.shakster.borgar.discord.entity.DiscordUser
import com.shakster.borgar.discord.entity.channel.DiscordMessageChannel
import com.shakster.borgar.discord.event.DiscordInteractionCommandEvent
import com.shakster.borgar.messaging.command.*
import com.shakster.borgar.messaging.entity.Attachment
import com.shakster.borgar.messaging.entity.FakeMessage
import com.shakster.borgar.messaging.event.CommandEvent
import com.shakster.borgar.messaging.executeAndRespond
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

suspend fun handleCommand(event: SlashCommandInteractionEvent) {
    handleBanned(event, "slash command") {
        return
    }

    val commandName = event.name
    val command = COMMANDS[commandName] ?: run {
        val entityId = event.guild?.id ?: event.user.id
        TemplateRepository.read(commandName, entityId)?.let(::TemplateCommand)
    }
    if (command == null) {
        logger.error("Unknown command: $commandName")
        event.reply("Unknown command!")
            .setEphemeral(true)
            .await()
        return
    }
    val arguments = DiscordOptionCommandArguments(event, command.defaultArgumentKey)
    val commandEvent = createSlashCommandEvent(event)
    executeSlashCommand(command, arguments, commandEvent, event)
}

private fun createSlashCommandEvent(event: SlashCommandInteractionEvent): CommandEvent {
    val manager = DiscordManager[event.jda]
    val discordChannel = event.channel
    val attachments = event.getOptionsByType(OptionType.ATTACHMENT).map {
        val attachment = it.asAttachment
        Attachment(
            id = attachment.id,
            url = attachment.url,
            proxyUrl = attachment.proxyUrl,
            filename = attachment.fileName,
            manager = manager,
            ephemeral = true,
        )
    }
    return DiscordInteractionCommandEvent(
        event,
        discordChannel,
        attachments,
    )
}

private suspend fun executeSlashCommand(
    command: Command,
    arguments: CommandArguments,
    commandEvent: CommandEvent,
    slashEvent: SlashCommandInteractionEvent,
) {
    if (command.guildOnly && slashEvent.guild == null) {
        logger.error("Guild only slash command $command used outside of a guild")
        slashEvent.reply("${command.nameWithPrefix} can only be used in a server!")
            .setEphemeral(true)
            .await()
        return
    }
    val commandPrefix = BotConfig.get().commandPrefix
    val afterCommands = arguments.getStringOrEmpty(AFTER_COMMANDS_ARGUMENT).let {
        if (it.isBlank()) it
        else if (!it.startsWith(commandPrefix)) "$commandPrefix$it"
        else it
    }
    val slashCommandConfig = CommandConfig(command, arguments).asSingletonList()
    val commandConfigs = if (afterCommands.isNotBlank()) {
        try {
            slashCommandConfig + getAfterCommandConfigs(afterCommands, commandEvent, slashEvent)
        } catch (e: CommandNotFoundException) {
            slashEvent.reply("The command **$commandPrefix${e.command}** does not exist!")
                .setEphemeral(true)
                .await()
            return
        }
    } else {
        slashCommandConfig
    }
    commandEvent.executeAndRespond(commandConfigs)
}

private suspend fun getAfterCommandConfigs(
    afterCommands: String,
    commandEvent: CommandEvent,
    slashEvent: SlashCommandInteractionEvent,
): List<CommandConfig> {
    val author = slashEvent.user
    val configs = parseCommands(
        afterCommands,
        FakeMessage(
            commandEvent.id,
            afterCommands,
            DiscordUser(author),
            DiscordMessageChannel(slashEvent.channel, slashEvent.context),
        ),
        author.id,
    )
    if (configs.isEmpty()) {
        val firstCommand = afterCommands.splitWords(limit = 2)
            .first()
            .substring(1)
        throw CommandNotFoundException(firstCommand)
    }
    return configs
}
