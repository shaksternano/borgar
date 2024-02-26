package io.github.shaksternano.borgar.chat.entity

interface User : DisplayedUser {

    val isSelf: Boolean

    suspend fun getBannerUrl(): String?
}
