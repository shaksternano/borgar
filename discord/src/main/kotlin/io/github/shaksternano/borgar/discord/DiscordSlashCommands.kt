package io.github.shaksternano.borgar.discord

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.updateCommands
import io.github.shaksternano.borgar.chat.command.*
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.formatted
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

suspend fun JDA.registerSlashCommands() {
    listener<SlashCommandInteractionEvent> {
        handleCommand(it)
    }
    updateCommands {
        val slashCommands = COMMANDS.values.map(Command::toSlash)
        addCommands(slashCommands)
    }.await()
}

private fun Command.toSlash(): SlashCommandData = Command(name, description) {
    isGuildOnly = guildOnly
    val discordPermissions = requiredPermissions.map { it.toDiscord() }
    defaultPermissions = DefaultMemberPermissions.enabledFor(discordPermissions)
    addOptions(argumentInfo.map(CommandArgumentInfo<*>::toOption))
    if (this@toSlash.chainable) addOptions(
        OptionData(
            OptionType.STRING,
            "aftercommands",
            "The commands to run after this one.",
            false,
        ),
    )
}

private suspend fun handleCommand(event: SlashCommandInteractionEvent) {
    val name = event.name
    val command = COMMANDS[name]
    if (command == null) {
        logger.error("Unknown command: $name")
        event.reply("Unknown command!").await()
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
                .await()
            return
        }
    } else {
        slashCommandConfig
    }
    val anyDefer = commandConfigs.any { it.command.deferReply }
    val anyEphemeral = commandConfigs.any { it.command.ephemeral }
    val deferReply =
        if (anyDefer) slashEvent.deferReply()
            .setEphemeral(anyEphemeral)
            .submit()
        else null
    val result = executeCommands(commandConfigs, commandEvent)
    val responses = result.first.map {
        it.copy(
            deferReply = anyDefer,
            ephemeral = anyEphemeral,
        )
    }
    val executable = result.second
    deferReply?.await()
    responses.send(executable, commandEvent)
}

private suspend fun getAfterCommandConfigs(
    afterCommands: String,
    commandEvent: CommandEvent,
    slashEvent: SlashCommandInteractionEvent,
): List<CommandConfig> {
    val configs = parseCommands(
        afterCommands,
        FakeMessage(
            commandEvent.id,
            commandEvent.manager,
            afterCommands,
            DiscordUser(slashEvent.user),
            DiscordMessageChannel(slashEvent.channel),
        ),
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
    val description = description + (defaultValue?.let {
        " Default value: ${it.formatted}"
    } ?: "")
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
            Int::class -> setRequiredRange((start as Int).toLong(), (end as Int).toLong())
            Long::class -> setRequiredRange(start as Long, end as Long)
            Float::class -> setRequiredRange((start as Float).toDouble(), (end as Float).toDouble())
            Double::class -> setRequiredRange(start as Double, end as Double)
            Number::class -> setRequiredRange((start as Number).toDouble(), (end as Number).toDouble())
        }
    }
}

private fun OptionData.setMinValue(validator: Validator<*>) {
    if (validator is MinValueValidator<*>) {
        val minValue = validator.minValue
        when (minValue.kClass) {
            Int::class -> setMinValue((minValue as Int).toLong())
            Long::class -> setMinValue(minValue as Long)
            Float::class -> setMinValue((minValue as Float).toDouble())
            Double::class -> setMinValue(minValue as Double)
            Number::class -> setMinValue((minValue as Number).toDouble())
        }
    }
}

private fun CommandArgumentType<*>.toOptionType(): OptionType = when (this) {
    CommandArgumentType.STRING -> OptionType.STRING
    CommandArgumentType.INTEGER -> OptionType.INTEGER
    CommandArgumentType.LONG -> OptionType.INTEGER
    CommandArgumentType.DOUBLE -> OptionType.NUMBER
    CommandArgumentType.BOOLEAN -> OptionType.BOOLEAN
    CommandArgumentType.USER -> OptionType.USER
    CommandArgumentType.CHANNEL -> OptionType.CHANNEL
    CommandArgumentType.ROLE -> OptionType.ROLE
    CommandArgumentType.MENTIONABLE -> OptionType.MENTIONABLE
    CommandArgumentType.ATTACHMENT -> OptionType.ATTACHMENT
}
