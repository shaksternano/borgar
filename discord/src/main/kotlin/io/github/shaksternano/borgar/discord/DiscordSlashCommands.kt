package io.github.shaksternano.borgar.discord

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.updateCommands
import io.github.shaksternano.borgar.chat.command.*
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.kClass
import io.github.shaksternano.borgar.core.util.splitWords
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
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
    val discordPermissions = requiredPermissions.map { it.toDiscord() }
    defaultPermissions = DefaultMemberPermissions.enabledFor(discordPermissions)
    addOptions(argumentInfo.map(CommandArgumentInfo<*>::toOption))
    if (this@toSlash is FileCommand) {
        addOptions(
            OptionData(
                OptionType.STRING,
                "aftercommands",
                "The commands to run after this one.",
                false,
            )
        )
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
    slashEvent: SlashCommandInteractionEvent,
) {
    val afterCommands = arguments.getStringOrEmpty("aftercommands").let {
        if (it.isBlank()) it
        else if (!it.startsWith(COMMAND_PREFIX)) "$COMMAND_PREFIX$it"
        else it
    }
    val slashCommandConfig = CommandConfig(command, arguments).asSingletonList()
    val commandConfigs = if (afterCommands.isNotBlank()) {
        try {
            slashCommandConfig + getAfterCommandConfigs(afterCommands, commandEvent, slashEvent)
        } catch (e: CommandNotFoundException) {
            slashEvent.reply("The command **$COMMAND_PREFIX${e.command}** does not exist!")
                .setEphemeral(true)
                .queue()
            return
        }
    } else {
        slashCommandConfig
    }
    val deferReply =
        if (command.deferReply) slashEvent.deferReply()
            .setEphemeral(command.ephemeral)
            .submit()
        else null
    val result = executeCommands(commandConfigs, commandEvent)
    val ephemeralWithFile = command.ephemeral && result.first.any {
        it.files.isNotEmpty()
    }
    val responses = if (ephemeralWithFile) {
        logger.error("Command ${command.name} tried to send a file in an ephemeral Discord message")
        CommandResponse(
            content = "An error occurred!",
            ephemeral = true,
            deferReply = command.deferReply,
        ).asSingletonList()
    } else {
        result.first.map {
            val singleResponse = result.first.size == 1
            it.copy(
                ephemeral = command.ephemeral && singleResponse,
                deferReply = command.deferReply
            )
        }
    }
    val executable = result.second
    deferReply?.await()
    sendResponse(responses, executable, commandEvent)
}

private suspend fun getAfterCommandConfigs(
    afterCommands: String,
    commandEvent: CommandEvent,
    slashEvent: SlashCommandInteractionEvent,
): List<CommandConfig> {
    val configs = parseCommands(
        FakeMessage(
            commandEvent.id,
            commandEvent.manager,
            afterCommands,
            DiscordUser(slashEvent.user),
            DiscordMessageChannel(slashEvent.channel),
        )
    )
    if (configs.isEmpty()) {
        val firstCommand = afterCommands.splitWords(limit = 2)
            .first()
            .substring(1)
        throw CommandNotFoundException(firstCommand)
    }
    return configs
}

private fun CommandArgumentInfo<*>.toOption(): OptionData {
    val optionData = OptionData(
        type.toOptionType(),
        key,
        description,
        required,
    )
    optionData.setRequiredRange(validator)
    optionData.setMinValue(validator)
    return optionData
}

private fun OptionData.setRequiredRange(validator: Validator<*>) {
    if (validator is RangeValidator<*>) {
        val range = validator.range
        val start = range.start
        val end = range.endInclusive
        when (start.kClass) {
            Long::class -> setRequiredRange(start as Long, end as Long)
            Double::class -> setRequiredRange(start as Double, end as Double)
        }
    }
}

private fun OptionData.setMinValue(validator: Validator<*>) {
    if (validator is MinValueValidator<*>) {
        val minValue = validator.minValue
        when (minValue.kClass) {
            Long::class -> setMinValue(minValue as Long)
            Double::class -> setMinValue(minValue as Double)
        }
    }
}

private fun CommandArgumentType<*>.toOptionType(): OptionType = when (this) {
    CommandArgumentType.STRING -> OptionType.STRING
    CommandArgumentType.LONG -> OptionType.INTEGER
    CommandArgumentType.DOUBLE -> OptionType.NUMBER
    CommandArgumentType.BOOLEAN -> OptionType.BOOLEAN
    CommandArgumentType.USER -> OptionType.USER
    CommandArgumentType.CHANNEL -> OptionType.CHANNEL
    CommandArgumentType.ROLE -> OptionType.ROLE
    CommandArgumentType.MENTIONABLE -> OptionType.MENTIONABLE
    CommandArgumentType.ATTACHMENT -> OptionType.ATTACHMENT
}
