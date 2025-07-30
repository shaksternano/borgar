package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.CatTask
import com.shakster.borgar.core.task.FileTask

class CatCommand(
    override val name: String,
    override val description: String,
    fileCount: Int,
) : ApiFilesCommand(
    fileCount,
) {

    companion object {
        val CAT: Command = CatCommand(
            name = "cat",
            description = "Sends a random cat image.",
            fileCount = 1,
        )

        val CAT_BOMB: Command = CatCommand(
            name = "catbomb",
            description = "Sends a bunch of random cat images.",
            fileCount = 10,
        )
    }

    override fun createApiFilesTask(tags: String, fileCount: Int, maxFileSize: Long): FileTask =
        CatTask(tags, fileCount, maxFileSize)
}
