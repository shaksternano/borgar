package io.github.shaksternano.borgar.chat.entity

interface DisplayedUser : Mentionable {

    val effectiveName: String
    val effectiveAvatarUrl: String
}
