package io.github.shaksternano.borgar.messaging.entity

interface CustomEmoji : Mentionable {

    override val name: String
    val imageUrl: String
}
