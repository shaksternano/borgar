package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.searchOrThrow
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.LazyUrlFileTask

object StickerImageCommand : FileCommand(
    requireInput = false,
) {

    override val name: String = "stickerimage"
    override val aliases: Set<String> = setOf("sticker")
    override val description: String = "Gets the image of a sticker."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        LazyUrlFileTask {
            event.asMessageIntersection(arguments)
                .searchOrThrow("No stickers found.") {
                    it.stickers.firstOrNull()?.imageUrl
                }
        }
}
