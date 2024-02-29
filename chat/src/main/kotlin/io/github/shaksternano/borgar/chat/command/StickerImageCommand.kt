package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.Sticker
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.searchOrThrow
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask

object StickerImageCommand : FileCommand(
    inputRequirement = InputRequirement.NotRequired,
) {

    override val name: String = "stickerimage"
    override val aliases: Set<String> = setOf("sticker")
    override val description: String = "Gets the image of a sticker."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val stickerUrls = event.asMessageIntersection(arguments)
            .searchOrThrow("No stickers found.") {
                it.stickers
                    .map(Sticker::imageUrl)
                    .ifEmpty {
                        null
                    }
            }
        return UrlFileTask(stickerUrls)
    }
}
