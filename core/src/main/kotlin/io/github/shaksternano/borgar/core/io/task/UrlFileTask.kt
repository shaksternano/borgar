package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource

class UrlFileTask(
    urls: Iterable<String>,
) : BaseFileTask() {

    constructor(url: String) : this(listOf(url))

    override val requireInput: Boolean = false
    private val urls = urls.map {
        DataSource.fromUrl(
            url = it,
            sendUrl = true,
        )
    }

    override suspend fun run(input: List<DataSource>): List<DataSource> = urls
}
