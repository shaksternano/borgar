package io.github.shaksternano.borgar.discord.command

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.updateCommands
import io.github.shaksternano.borgar.core.data.repository.TemplateRepository
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.*
import io.github.shaksternano.borgar.discord.await
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
import io.github.shaksternano.borgar.discord.event.DiscordMessageInteractionEvent
import io.github.shaksternano.borgar.discord.event.DiscordUserInteractionEvent
import io.github.shaksternano.borgar.discord.event.SlashCommandEvent
import io.github.shaksternano.borgar.discord.util.toDiscord
import io.github.shaksternano.borgar.messaging.command.*
import io.github.shaksternano.borgar.messaging.entity.*
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import io.github.shaksternano.borgar.messaging.event.MessageInteractionEvent
import io.github.shaksternano.borgar.messaging.event.UserInteractionEvent
import io.github.shaksternano.borgar.messaging.interaction.message.MESSAGE_INTERACTION_COMMANDS
import io.github.shaksternano.borgar.messaging.interaction.message.MessageInteractionCommand
import io.github.shaksternano.borgar.messaging.interaction.message.handleMessageInteraction
import io.github.shaksternano.borgar.messaging.interaction.user.USER_INTERACTION_COMMANDS
import io.github.shaksternano.borgar.messaging.interaction.user.UserInteractionCommand
import io.github.shaksternano.borgar.messaging.interaction.user.handleUserInteraction
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

const val AFTER_COMMANDS_ARGUMENT = "aftercommands"

suspend fun JDA.registerCommands() {
    listener<SlashCommandInteractionEvent> {
        handleCommand(it)
    }
    listener<MessageContextInteractionEvent> {
        handleMessageInteraction(it.convert())
    }
    listener<UserContextInteractionEvent> {
        handleUserInteraction(it.convert())
    }
    updateCommands {
        val slashCommands = COMMANDS.values
            .map(Command::toSlash)
        addCommands(slashCommands)
        val messageInteractionCommands = MESSAGE_INTERACTION_COMMANDS.values
            .map(MessageInteractionCommand::toDiscord)
        addCommands(messageInteractionCommands)
        val userInteractionCommands = USER_INTERACTION_COMMANDS.values
            .map(UserInteractionCommand::toDiscord)
        addCommands(userInteractionCommands)
    }.await()
}

fun Command.toSlash(): SlashCommandData = Command(name, description) {
    setContexts(environment.toDiscord())
    setIntegrationTypes(IntegrationType.ALL)
    val discordPermissions = requiredPermissions.map { it.toDiscord() }
    defaultPermissions = DefaultMemberPermissions.enabledFor(discordPermissions)
    addOptions(argumentInfo.map(CommandArgumentInfo<*>::toOption))
    if (this@toSlash.chainable) addOptions(
        OptionData(
            OptionType.STRING,
            AFTER_COMMANDS_ARGUMENT,
            "The commands to run after this one.",
            false,
        ),
    )
}

private fun MessageInteractionCommand.toDiscord(): CommandData =
    Commands.message(name)
        .setContexts(environment.toDiscord())
        .setIntegrationTypes(IntegrationType.ALL)

private fun UserInteractionCommand.toDiscord(): CommandData =
    Commands.user(name)
        .setContexts(environment.toDiscord())
        .setIntegrationTypes(IntegrationType.ALL)

private fun Iterable<ChannelEnvironment>.toDiscord(): List<InteractionContextType> =
    map(ChannelEnvironment::toDiscord)

private fun ChannelEnvironment.toDiscord(): InteractionContextType = when (this) {
    ChannelEnvironment.GUILD -> InteractionContextType.GUILD
    ChannelEnvironment.DIRECT_MESSAGE -> InteractionContextType.BOT_DM
    ChannelEnvironment.PRIVATE -> InteractionContextType.PRIVATE_CHANNEL
    ChannelEnvironment.GROUP -> InteractionContextType.PRIVATE_CHANNEL
}

private fun MessageContextInteractionEvent.convert(): MessageInteractionEvent =
    DiscordMessageInteractionEvent(this)

private fun UserContextInteractionEvent.convert(): UserInteractionEvent =
    DiscordUserInteractionEvent(this)

private suspend fun handleCommand(event: SlashCommandInteractionEvent) {
    val commandName = event.name
    val command = COMMANDS[commandName] ?: run {
        val entityId = event.guild?.id ?: event.user.id
        TemplateRepository.read(commandName, entityId)?.let(::TemplateCommand)
    }
    if (command == null) {
        logger.error("Unknown command: $commandName")
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
    if (command.guildOnly && slashEvent.guild == null) {
        logger.error("Guild only slash command $command used outside of a guild")
        slashEvent.reply("${command.nameWithPrefix} can only be used in a server!")
            .setEphemeral(true)
            .await()
        return
    }
    val afterCommands = arguments.getStringOrEmpty(AFTER_COMMANDS_ARGUMENT).let {
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
    val environment = commandEvent.getChannel().environment
    commandEvent.ephemeralReply = commandConfigs.any { it.command.ephemeralReply }
    val (responses, executable) = coroutineScope {
        val anyDefer = commandConfigs.any { it.command.deferReply }
        if (anyDefer) launch {
            commandEvent.deferReply()
        }
        executeCommands(commandConfigs, environment, commandEvent)
    }
    sendResponses(responses, executable, commandEvent)
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
    val argumentType = type
    if (argumentType is CommandArgumentType.Enum<*>) {
        val choices = argumentType.values.map {
            it as Displayed
            Choice(it.displayName, it.ordinal.toLong())
        }
        optionData.addChoices(choices)
    }
    return optionData
}

private fun OptionData.setRequiredRange(validator: Validator<*>) {
    if (validator is RangeValidator<*>) {
        val range = validator.range
        val end = range.endInclusive
        when (val start = range.start) {
            is Int -> setRequiredRange(start.toLong(), (end as Int).toLong())
            is Long -> setRequiredRange(start, end as Long)
            is Float -> setRequiredRange(start.toDouble(), (end as Float).toDouble())
            is Double -> setRequiredRange(start, end as Double)
            is Number -> setRequiredRange(start.toDouble(), (end as Number).toDouble())
        }
    }
}

private fun OptionData.setMinValue(validator: Validator<*>) {
    if (validator is MinValueValidator<*>) {
        when (val minValue = validator.minValue) {
            is Int -> setMinValue(minValue.toLong())
            is Long -> setMinValue(minValue)
            is Float -> setMinValue(minValue.toDouble())
            is Double -> setMinValue(minValue)
            is Number -> setMinValue(minValue.toDouble())
        }
    }
}

private fun CommandArgumentType<*>.toOptionType(): OptionType = when (this) {
    CommandArgumentType.String -> OptionType.STRING
    CommandArgumentType.Integer -> OptionType.INTEGER
    CommandArgumentType.Long -> OptionType.INTEGER
    CommandArgumentType.Double -> OptionType.NUMBER
    CommandArgumentType.Boolean -> OptionType.BOOLEAN
    CommandArgumentType.User -> OptionType.USER
    CommandArgumentType.Channel -> OptionType.CHANNEL
    CommandArgumentType.Role -> OptionType.ROLE
    CommandArgumentType.Mentionable -> OptionType.MENTIONABLE
    CommandArgumentType.Attachment -> OptionType.ATTACHMENT
    is CommandArgumentType.Enum<*> -> OptionType.STRING
}
