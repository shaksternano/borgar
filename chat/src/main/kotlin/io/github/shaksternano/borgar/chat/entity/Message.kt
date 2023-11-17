package io.github.shaksternano.borgar.chat.entity

import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import kotlinx.coroutines.flow.Flow

interface Message : CommandMessageIntersection {

    val mentionedUsers: Flow<User>
    val mentionedChannels: Flow<Channel>
    val mentionedRoles: Flow<Role>

    val mentionedUserIds: Set<Mentionable>
    val mentionedChannelIds: Set<Mentionable>
    val mentionedRoleIds: Set<Mentionable>
}
