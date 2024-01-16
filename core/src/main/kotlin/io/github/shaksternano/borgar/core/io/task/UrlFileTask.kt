package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.UrlDataSource
import io.github.shaksternano.borgar.core.io.filename
import io.github.shaksternano.borgar.core.util.asSingletonList

class UrlFileTask(
    private val url: String,
) : BaseFileTask(
    requireInput = false,
) {

    override suspend fun run(input: List<DataSource>): List<DataSource> {
        return UrlDataSource(
            filename(url),
            url,
            sendUrl = true
        ).asSingletonList()
    }
}
