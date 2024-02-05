package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.util.asSingletonList

class UrlFileTask(
    url: String,
) : BaseFileTask(
    requireInput = false,
) {

    private val urls = DataSource.fromUrl(
        url = url,
        sendUrl = true
    ).asSingletonList()

    override suspend fun run(input: List<DataSource>): List<DataSource> = urls
}
