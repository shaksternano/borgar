package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.getEmojiAndUrlDrawables
import io.github.shaksternano.borgar.core.io.task.CaptionTask
import io.github.shaksternano.borgar.core.io.task.FileTask

sealed class CaptionCommand(
    override val name: String,
    private val isCaption2: Boolean,
) : FileCommand(
    CommandArgumentInfo(
        key = "caption",
        description = "The caption text",
        type = CommandArgumentType.String,
    )
) {

    object Caption : CaptionCommand(
        "caption",
        false,
    )

    object Caption2 : CaptionCommand(
        "caption2",
        true,
    )

    override val description: String =
        "Captions a media file."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        CaptionTask(
            arguments.getDefaultStringOrEmpty(),
            isCaption2,
            event.asMessageIntersection(arguments).getEmojiAndUrlDrawables(),
            maxFileSize,
        )
}
