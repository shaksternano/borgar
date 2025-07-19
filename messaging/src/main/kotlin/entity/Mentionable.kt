package io.github.shaksternano.borgar.messaging.entity

interface Mentionable : Entity {

    val asMention: String
    val asBasicMention: String
}
