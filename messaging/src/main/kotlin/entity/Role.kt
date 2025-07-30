package com.shakster.borgar.messaging.entity

interface Role : Mentionable, PermissionHolder {

    override val name: String
}
