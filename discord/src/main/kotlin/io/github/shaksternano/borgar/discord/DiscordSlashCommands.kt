package io.github.shaksternano.borgar.discord

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import io.github.shaksternano.borgar.chat.command.*
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.exception.CommandException
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.discord.event.SlashCommandEvent
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

fun JDA.registerSlashCommands() {
    listener<SlashCommandInteractionEvent> {
        handleCommand(it)
    }
    updateCommands {
        COMMANDS.values.fold(this) { updateAction, command ->
            updateAction.slash(command.name, command.description) {
                isGuildOnly = command.guildOnly
                val discordPermissions = command.requiredPermissions.map { getDiscordPermission(it) }
                defaultPermissions = DefaultMemberPermissions.enabledFor(discordPermissions)
                addOptions(command.argumentData.map(CommandArgumentData::toOption))
            }
        }.queue()
    }
}

private suspend fun handleCommand(event: SlashCommandInteractionEvent) {
    val name = event.name
    val command = COMMANDS[name]
    if (command == null) {
        logger.error("Unknown command: $name")
        event.reply("Unknown command!").queue()
        return
    }
    val arguments = OptionCommandArguments(event, command.defaultArgumentKey)
    val commandEvent = SlashCommandEvent(event)
    executeCommand(command, arguments, commandEvent, event)
}

private suspend fun executeCommand(
    command: Command,
    arguments: CommandArguments,
    commandEvent: CommandEvent,
    slashEvent: SlashCommandInteractionEvent
) {
    val deferReply = slashEvent.deferReply().submit()
    val executable = try {
        command.run(arguments, commandEvent)
    } catch (t: Throwable) {
        throw CommandException(command, t)
    }
    val responses = try {
        executable.execute()
    } catch (t: Throwable) {
        throw CommandException(command, t)
    }
    deferReply.await()
    sendResponse(responses, executable, commandEvent)
}

private fun CommandArgumentData.toOption(): OptionData = OptionData(
    type.toOptionType(),
    key,
    description,
    required,
)

private fun CommandArgumentType.toOptionType(): OptionType = when (this) {
    CommandArgumentType.STRING -> OptionType.STRING
    CommandArgumentType.LONG -> OptionType.INTEGER
    CommandArgumentType.DOUBLE -> OptionType.NUMBER
    CommandArgumentType.BOOLEAN -> OptionType.BOOLEAN
    CommandArgumentType.USER -> OptionType.USER
    CommandArgumentType.CHANNEL -> OptionType.CHANNEL
    CommandArgumentType.ROLE -> OptionType.ROLE
    CommandArgumentType.MENTIONABLE -> OptionType.MENTIONABLE
    CommandArgumentType.ATTACHMENT -> OptionType.STRING
    else -> OptionType.UNKNOWN
}
