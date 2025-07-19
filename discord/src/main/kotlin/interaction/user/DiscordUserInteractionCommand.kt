package io.github.shaksternano.borgar.discord.interaction.user

import io.github.shaksternano.borgar.discord.interaction.DiscordInteractionCommand
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

interface DiscordUserInteractionCommand : DiscordInteractionCommand<UserContextInteractionEvent>
