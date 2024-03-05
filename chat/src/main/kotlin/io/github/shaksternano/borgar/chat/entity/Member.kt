package io.github.shaksternano.borgar.chat.entity

interface Member : DisplayedUser, PermissionHolder {

    val user: User

    suspend fun getGuild(): Guild
}
