package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.PonyTask

class PonyCommand(
    override val name: String,
    override val description: String,
    fileCount: Int,
) : ApiFilesCommand(
    name,
    description,
    fileCount,
) {

    companion object {
        val PONY: Command = PonyCommand(
            name = "pony",
            description = "Sends a random pony image.",
            fileCount = 1,
        )

        val PONY_BOMB: Command = PonyCommand(
            name = "ponybomb",
            description = "Sends a bunch of random pony images.",
            fileCount = 10,
        )
    }

    override fun createApiFilesTask(tags: String, fileCount: Int, maxFileSize: Long): FileTask =
        PonyTask(tags, fileCount, maxFileSize)
}
