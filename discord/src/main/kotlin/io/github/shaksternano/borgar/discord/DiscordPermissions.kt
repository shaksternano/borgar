package io.github.shaksternano.borgar.discord

import io.github.shaksternano.borgar.chat.command.Permission

fun Permission.toDiscord(): net.dv8tion.jda.api.Permission = when (this) {
    Permission.MANAGE_GUILD_EXPRESSIONS -> net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS
}
