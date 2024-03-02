package io.github.shaksternano.borgar.chat.entity

interface Member : DisplayedUser, PermissionHolder {

    val user: User
    val guild: Guild
}
