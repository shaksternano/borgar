package com.shakster.borgar.discord.interaction.user

import com.shakster.borgar.discord.interaction.DiscordInteractionCommand
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

interface DiscordUserInteractionCommand : DiscordInteractionCommand<UserContextInteractionEvent>
