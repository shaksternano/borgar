package com.shakster.borgar.messaging.entity

import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.io.DataSourceConvertable
import com.shakster.borgar.core.io.UrlDataSource
import com.shakster.borgar.messaging.BotManager

data class Attachment(
    override val id: String,
    val url: String,
    val proxyUrl: String?,
    val filename: String,
    override val manager: BotManager,
    val ephemeral: Boolean = false,
) : BaseEntity(), DataSourceConvertable {

    override val name: String = filename

    override fun asDataSource(): UrlDataSource = DataSource.fromUrl(url, filename)
}
