package io.github.shaksternano.borgar.chat.entity

interface Member : DisplayedUser {

    val user: User
    val guild: Guild
}
