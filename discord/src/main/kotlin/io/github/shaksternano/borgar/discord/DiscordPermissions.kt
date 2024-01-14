package io.github.shaksternano.borgar.discord

import io.github.shaksternano.borgar.chat.command.Permission

fun getDiscordPermission(permission: Permission): net.dv8tion.jda.api.Permission = when (permission) {
    Permission.MANAGE_GUILD_EXPRESSIONS -> net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS
    else -> net.dv8tion.jda.api.Permission.UNKNOWN
}
