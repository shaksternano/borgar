package io.github.shaksternano.borgar.discord.command

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.updateCommands
import io.github.shaksternano.borgar.core.data.repository.BanRepository
import io.github.shaksternano.borgar.core.data.repository.EntityType
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.core.util.Displayed
import io.github.shaksternano.borgar.core.util.MessagingPlatform
import io.github.shaksternano.borgar.core.util.formatted
import io.github.shaksternano.borgar.discord.interaction.message.DiscordMessageInteractionCommand
import io.github.shaksternano.borgar.discord.interaction.message.MESSAGE_INTERACTION_COMMANDS
import io.github.shaksternano.borgar.discord.interaction.message.handleMessageInteraction
import io.github.shaksternano.borgar.discord.interaction.modal.handleModalInteraction
import io.github.shaksternano.borgar.discord.interaction.user.DiscordUserInteractionCommand
import io.github.shaksternano.borgar.discord.interaction.user.USER_INTERACTION_COMMANDS
import io.github.shaksternano.borgar.discord.interaction.user.handleUserInteraction
import io.github.shaksternano.borgar.discord.util.toDiscord
import io.github.shaksternano.borgar.messaging.command.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

const val AFTER_COMMANDS_ARGUMENT: String = "aftercommands"

suspend fun JDA.registerCommands() {
    listener<SlashCommandInteractionEvent> {
        handleCommand(it)
    }
    listener<CommandAutoCompleteInteractionEvent> {
        handleCommandAutoComplete(it)
    }
    listener<MessageContextInteractionEvent> {
        handleMessageInteraction(it)
    }
    listener<UserContextInteractionEvent> {
        handleUserInteraction(it)
    }
    listener<ModalInteractionEvent> {
        handleModalInteraction(it)
    }
    registerAutoCompleteHandlers()
    updateCommands {
        val slashCommands = COMMANDS.values
            .map(Command::toSlash)
        addCommands(slashCommands)

        val messageInteractionCommands = MESSAGE_INTERACTION_COMMANDS.values
            .map(DiscordMessageInteractionCommand::toCommandData)
        addCommands(messageInteractionCommands)

        val userInteractionCommands = USER_INTERACTION_COMMANDS.values
            .map(DiscordUserInteractionCommand::toCommandData)
        addCommands(userInteractionCommands)
    }.await()
}

inline fun handleBanned(event: Interaction, type: String, ifBanned: () -> Unit) {
    val userId = event.user.id
    if (BanRepository.exists(
            userId,
            EntityType.USER,
            MessagingPlatform.DISCORD,
        )
    ) {
        var message = "Ignoring $type from banned user \"${event.user.name}\" ($userId)"
        val channelId = event.channelId
        if (channelId != null) {
            val channel = event.channel
            message += if (channel == null) {
                " sent in channel $channelId"
            } else {
                " sent in channel \"${channel.name}\" ($channelId)"
            }
        }
        message += " on ${MessagingPlatform.DISCORD.displayName}"
        logger.info(message)
        ifBanned()
    }
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

private fun DiscordMessageInteractionCommand.toCommandData(): CommandData =
    Commands.message(name)
        .setContexts(environment)
        .setIntegrationTypes(IntegrationType.ALL)

private fun DiscordUserInteractionCommand.toCommandData(): CommandData =
    Commands.user(name)
        .setContexts(environment)
        .setIntegrationTypes(IntegrationType.ALL)

private fun Iterable<ChannelEnvironment>.toDiscord(): List<InteractionContextType> =
    map(ChannelEnvironment::toDiscord)

private fun ChannelEnvironment.toDiscord(): InteractionContextType = when (this) {
    ChannelEnvironment.GUILD -> InteractionContextType.GUILD
    ChannelEnvironment.DIRECT_MESSAGE -> InteractionContextType.BOT_DM
    ChannelEnvironment.PRIVATE -> InteractionContextType.PRIVATE_CHANNEL
    ChannelEnvironment.GROUP -> InteractionContextType.PRIVATE_CHANNEL
}

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
