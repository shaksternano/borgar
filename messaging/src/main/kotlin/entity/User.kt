package io.github.shaksternano.borgar.messaging.entity

interface User : DisplayedUser {

    override val name: String
    val isSelf: Boolean
    val isBot: Boolean

    suspend fun getBannerUrl(): String?
}
