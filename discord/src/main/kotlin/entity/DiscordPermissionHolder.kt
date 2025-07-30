package com.shakster.borgar.discord.entity

import com.shakster.borgar.discord.entity.channel.DiscordChannel
import com.shakster.borgar.discord.util.toDiscord
import com.shakster.borgar.messaging.command.Permission
import com.shakster.borgar.messaging.entity.BaseEntity
import com.shakster.borgar.messaging.entity.PermissionHolder
import com.shakster.borgar.messaging.entity.channel.Channel
import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.detached.IDetachableEntity

abstract class DiscordPermissionHolder(
    private val permissionHolder: IPermissionHolder,
) : PermissionHolder, BaseEntity() {

    private val isDetached: Boolean = permissionHolder is IDetachableEntity && permissionHolder.isDetached

    override suspend fun hasPermission(permissions: Set<Permission>): Boolean =
        if (isDetached) {
            true
        } else {
            permissionHolder.hasPermission(permissions.map { it.toDiscord() })
        }

    override suspend fun hasPermission(permissions: Set<Permission>, channel: Channel): Boolean =
        if (isDetached) {
            true
        } else if (channel is DiscordChannel && channel.discordChannel is GuildChannel) {
            permissionHolder.hasPermission(channel.discordChannel, permissions.map { it.toDiscord() })
        } else {
            hasPermission(permissions)
        }
}
