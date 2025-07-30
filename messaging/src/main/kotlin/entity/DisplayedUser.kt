package com.shakster.borgar.messaging.entity

interface DisplayedUser : Mentionable {

    val effectiveName: String
    val effectiveAvatarUrl: String
}
