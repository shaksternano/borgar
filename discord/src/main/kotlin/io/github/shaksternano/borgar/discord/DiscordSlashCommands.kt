package io.github.shaksternano.borgar.discord

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.updateCommands
import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.command.*
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
import io.github.shaksternano.borgar.discord.event.SlashCommandEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
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
    val deferReply = slashEvent.deferReply().submit()
    val (responses, executable) = executeCommands(commandConfigs, commandEvent)
    deferReply.await()
    sendResponse(responses, executable, commandEvent)
}

private suspend fun getAfterCommandConfigs(
    afterCommands: String,
    commandEvent: CommandEvent,
    slashEvent: SlashCommandInteractionEvent,
): List<CommandConfig> {
    val configs = parseCommands(
        DummyMessage(
            commandEvent.id,
            commandEvent.manager,
            afterCommands,
            DiscordUser(slashEvent.user),
            DiscordMessageChannel(slashEvent.channel),
        )
    )
    if (configs.isEmpty()) {
        val firstCommand = afterCommands.split(" ", limit = 2).first().substring(1)
        throw CommandNotFoundException(firstCommand)
    }
    return configs
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
    SimpleCommandArgumentType.ATTACHMENT -> OptionType.ATTACHMENT
}

private class DummyMessage(
    override val id: String,
    override val manager: BotManager,
    override val content: String,
    private val author: User,
    private val channel: MessageChannel,
) : Message {

    private val mentionedUsersSet: Set<User> = manager.getMentionedUsers(content).toSet()
    private val mentionedChannelsSet: Set<Channel> = manager.getMentionedChannels(content).toSet()
    private val mentionedRolesSet: Set<Role> = manager.getMentionedRoles(content).toSet()

    override val mentionedUsers: Flow<User> = mentionedUsersSet.asFlow()
    override val mentionedChannels: Flow<Channel> = mentionedChannelsSet.asFlow()
    override val mentionedRoles: Flow<Role> = mentionedRolesSet.asFlow()

    override val mentionedUserIds: Set<Mentionable> = mentionedUsersSet
    override val mentionedChannelIds: Set<Mentionable> = mentionedChannelsSet
    override val mentionedRoleIds: Set<Mentionable> = mentionedRolesSet

    override val attachments: List<Attachment> = listOf()
    override val embeds: List<MessageEmbed> = listOf()
    override val customEmojis: List<CustomEmoji> = manager.getCustomEmojis(content)

    override suspend fun getAuthor(): User = author

    override suspend fun getChannel(): MessageChannel = channel

    override suspend fun getGuild(): Guild? = channel.getGuild()

    override suspend fun getReferencedMessage(): Message? = null
}
