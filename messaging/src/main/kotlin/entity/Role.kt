package io.github.shaksternano.borgar.messaging.entity

interface Role : Mentionable, PermissionHolder {

    override val name: String
}
