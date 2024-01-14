package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.getEmojiDrawables
import io.github.shaksternano.borgar.core.io.task.CaptionTask
import io.github.shaksternano.borgar.core.io.task.FileTask

sealed class CaptionCommand(
    override val name: String,
    private val isCaption2: Boolean,
) : FileCommand() {

    override val description: String =
        "Captions a media file."
    override val argumentData: Set<CommandArgumentData> = setOf(
        CommandArgumentData(
            "caption",
            "The caption text",
            CommandArgumentType.STRING,
            false
        )
    )

    object Caption : CaptionCommand(
        "caption",
        false,
    )

    object Caption2 : CaptionCommand(
        "caption2",
        true,
    )

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        return CaptionTask(
            arguments.getDefaultStringOrEmpty(),
            isCaption2,
            event.asMessageIntersection(arguments).getEmojiDrawables(),
            maxFileSize,
        )
    }
}
