package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.util.asSingletonList

class LazyUrlFileTask(
    private val initializer: suspend () -> String,
) : BaseFileTask(
    requireInput = false,
) {

    override suspend fun run(input: List<DataSource>): List<DataSource> =
        DataSource.fromUrl(
            url = initializer(),
            sendUrl = true,
        ).asSingletonList()
}
