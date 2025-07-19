package io.github.shaksternano.borgar.messaging.entity

interface Role : Mentionable, PermissionHolder {

    val name: String
}
