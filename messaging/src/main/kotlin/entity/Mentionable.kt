package com.shakster.borgar.messaging.entity

interface Mentionable : Entity {

    val asMention: String
    val asBasicMention: String
}
