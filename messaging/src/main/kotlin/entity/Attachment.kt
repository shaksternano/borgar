package io.github.shaksternano.borgar.messaging.entity

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.DataSourceConvertable
import io.github.shaksternano.borgar.core.io.UrlDataSource
import io.github.shaksternano.borgar.messaging.BotManager

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
