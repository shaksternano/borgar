package io.github.shaksternano.borgar.chat.entity

interface Role : Mentionable, PermissionHolder {

    val name: String
}
