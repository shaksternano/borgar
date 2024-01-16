package io.github.shaksternano.borgar.discord

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.Command
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
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

fun JDA.registerSlashCommands() {
    listener<SlashCommandInteractionEvent> {
        handleCommand(it)
    }
    updateCommands {
        val slashCommands = COMMANDS.values.map(Command::toSlash)
        addCommands(slashCommands)
    }.queue()
}

private fun Command.toSlash(): SlashCommandData = Command(name, description) {
    isGuildOnly = guildOnly
    val discordPermissions = requiredPermissions.map { getDiscordPermission(it) }
    defaultPermissions = DefaultMemberPermissions.enabledFor(discordPermissions)
    addOptions(argumentInfo.map(CommandArgumentInfo<*>::toOption))
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
    val (responses, executable) = try {
        val executable = try {
            command.run(arguments, commandEvent)
        } catch (t: Throwable) {
            throw CommandException(command, t)
        }
        val result = try {
            executable.execute()
        } catch (t: Throwable) {
            throw CommandException(command, t)
        }
        result to executable
    } catch (t: Throwable) {
        val responseContent = handleError(t, commandEvent.manager)
        listOf(CommandResponse(responseContent)) to null
    }
    runCatching {
        deferReply.await()
    }
    sendResponse(responses, executable, commandEvent)
}

private fun CommandArgumentInfo<*>.toOption(): OptionData = OptionData(
    type.toOptionType(),
    key,
    description,
    required,
)

private fun CommandArgumentType<*>.toOptionType(): OptionType = when (this) {
    SimpleCommandArgumentType.STRING -> OptionType.STRING
    SimpleCommandArgumentType.LONG -> OptionType.INTEGER
    SimpleCommandArgumentType.DOUBLE -> OptionType.NUMBER
    SimpleCommandArgumentType.BOOLEAN -> OptionType.BOOLEAN
    SuspendingCommandArgumentType.USER -> OptionType.USER
    SuspendingCommandArgumentType.CHANNEL -> OptionType.CHANNEL
    SuspendingCommandArgumentType.ROLE -> OptionType.ROLE
    SimpleCommandArgumentType.MENTIONABLE -> OptionType.MENTIONABLE
    SimpleCommandArgumentType.ATTACHMENT -> OptionType.STRING
    else -> OptionType.UNKNOWN
}
