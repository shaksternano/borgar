package io.github.shaksternano.borgar.chat.entity

import io.github.shaksternano.borgar.chat.BotManager

data class Attachment(
    override val id: String,
    val url: String,
    val proxyUrl: String,
    val fileName: String,
    override val manager: BotManager,
) : BaseEntity()
