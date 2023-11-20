package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.chat.command.Permission
import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.PermissionHolder
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.discord.entity.channel.DiscordChannel
import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel

abstract class DiscordPermissionHolder(
    private val permissionHolder: IPermissionHolder,
) : PermissionHolder, BaseEntity() {

    override suspend fun hasPermission(permissions: Iterable<Permission>): Boolean =
        permissionHolder.hasPermission(permissions.map { getDiscordPermission(it) })

    override suspend fun hasPermission(permissions: Iterable<Permission>, channel: Channel): Boolean =
        if (channel is DiscordChannel && channel.discordChannel is GuildChannel) {
            permissionHolder.hasPermission(channel.discordChannel, permissions.map { getDiscordPermission(it) })
        } else {
            hasPermission(permissions)
        }

    private fun getDiscordPermission(permission: Permission): net.dv8tion.jda.api.Permission = when (permission) {
        Permission.MANAGE_GUILD_EXPRESSIONS -> net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS
        else -> net.dv8tion.jda.api.Permission.UNKNOWN
    }
}
