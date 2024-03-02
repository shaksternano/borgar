package io.github.shaksternano.borgar.chat.entity

interface Mentionable : Entity {

    val asMention: String
    val asBasicMention: String
}
