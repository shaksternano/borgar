package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.searchOrThrow
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet

object StickerImageCommand : FileCommand(
    inputRequirement = InputRequirement.None,
) {

    override val name: String = "stickerimage"
    override val aliases: Set<String> = setOf("sticker")
    override val description: String = "Gets the image of a sticker."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val stickerUrls = event.asMessageIntersection(arguments)
            .searchOrThrow("No stickers found.") { message ->
                message.stickers
                    .map { it.imageUrl }
                    .toSet()
                    .ifEmpty {
                        null
                    }
            }
        return UrlFileTask(stickerUrls)
    }
}
