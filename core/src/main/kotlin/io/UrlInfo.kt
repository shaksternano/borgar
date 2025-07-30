package com.shakster.borgar.core.io

data class UrlInfo(
    val url: String,
    val filename: String = filename(url),
    val gifv: Boolean = false,
) : DataSourceConvertable {

    override fun asDataSource(): UrlDataSource = DataSource.fromUrl(url, filename)
}
