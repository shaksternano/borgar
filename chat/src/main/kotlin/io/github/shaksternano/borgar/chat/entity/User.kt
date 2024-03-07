package io.github.shaksternano.borgar.chat.entity

interface User : DisplayedUser {

    val name: String
    val isSelf: Boolean
    val isBot: Boolean

    suspend fun getBannerUrl(): String?
}
