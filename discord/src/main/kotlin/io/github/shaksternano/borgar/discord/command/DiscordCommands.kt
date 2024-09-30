package io.github.shaksternano.borgar.discord.command

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.updateCommands
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.*
import io.github.shaksternano.borgar.discord.event.DiscordMessageInteractionEvent
import io.github.shaksternano.borgar.discord.event.DiscordUserInteractionEvent
import io.github.shaksternano.borgar.discord.util.toDiscord
import io.github.shaksternano.borgar.messaging.command.*
import io.github.shaksternano.borgar.messaging.entity.*
import io.github.shaksternano.borgar.messaging.event.MessageInteractionEvent
import io.github.shaksternano.borgar.messaging.event.UserInteractionEvent
import io.github.shaksternano.borgar.messaging.interaction.message.MESSAGE_INTERACTION_COMMANDS
import io.github.shaksternano.borgar.messaging.interaction.message.MessageInteractionCommand
import io.github.shaksternano.borgar.messaging.interaction.message.handleMessageInteraction
import io.github.shaksternano.borgar.messaging.interaction.user.USER_INTERACTION_COMMANDS
import io.github.shaksternano.borgar.messaging.interaction.user.UserInteractionCommand
import io.github.shaksternano.borgar.messaging.interaction.user.handleUserInteraction
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
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
    listener<CommandAutoCompleteInteractionEvent> {
        handleCommandAutoComplete(it)
    }
    val commandModalInteractionName = "Run Command"
    listener<MessageContextInteractionEvent> {
        if (it.name == commandModalInteractionName) {
            createCommandModal(it)
        } else {
            handleMessageInteraction(it.convert())
        }
    }
    listener<UserContextInteractionEvent> {
        handleUserInteraction(it.convert())
    }
    listener<ModalInteractionEvent> {
        if (it.modalId == COMMAND_MODAL_ID) {
            handleModalCommand(it)
        } else {
            logger.error("Unknown modal ID: ${it.modalId}")
        }
    }
    registerAutoCompleteHandlers()
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
        val commandModalInteraction = Commands.message(commandModalInteractionName)
            .setContexts(InteractionContextType.ALL)
            .setIntegrationTypes(IntegrationType.ALL)
        addCommands(commandModalInteraction)
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

private fun CommandArgumentInfo<*>.toOption(): OptionData {
    val description = description + (defaultValue?.let {
        " Default value: ${it.formatted}"
    } ?: "")
    val hasAutoComplete = autoCompleteHandler != null
    val optionData = OptionData(
        type.toOptionType(),
        key,
        description,
        required,
        hasAutoComplete,
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
