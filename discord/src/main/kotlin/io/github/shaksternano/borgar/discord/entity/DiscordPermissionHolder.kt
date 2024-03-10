package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.discord.entity.channel.DiscordChannel
import io.github.shaksternano.borgar.discord.util.toDiscord
import io.github.shaksternano.borgar.messaging.command.Permission
import io.github.shaksternano.borgar.messaging.entity.BaseEntity
import io.github.shaksternano.borgar.messaging.entity.PermissionHolder
import io.github.shaksternano.borgar.messaging.entity.channel.Channel
import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel

abstract class DiscordPermissionHolder(
    private val permissionHolder: IPermissionHolder,
) : PermissionHolder, BaseEntity() {

    override suspend fun hasPermission(permissions: Set<Permission>): Boolean =
        permissionHolder.hasPermission(permissions.map { it.toDiscord() })

    override suspend fun hasPermission(permissions: Set<Permission>, channel: Channel): Boolean =
        if (channel is DiscordChannel && channel.discordChannel is GuildChannel) {
            permissionHolder.hasPermission(channel.discordChannel, permissions.map { it.toDiscord() })
        } else {
            hasPermission(permissions)
        }
}
