package io.github.shaksternano.borgar.messaging.entity

interface Sticker : Entity {

    override val name: String
    val imageUrl: String
}
