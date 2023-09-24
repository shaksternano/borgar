package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.CaptionTask
import io.github.shaksternano.borgar.core.io.task.FileTask

sealed class CaptionCommand(
    override val name: String,
    private val isCaption2: Boolean,
) : FileCommand() {

    override val description: String =
        "Captions a media file. Optional arguments: [Caption text]"
    override val defaultArgumentKey: String = "caption"

    object Caption : CaptionCommand(
        "caption",
        false,
    )

    object Caption2 : CaptionCommand(
        "caption2",
        true,
    )

    override fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        return CaptionTask(
            arguments.getString("caption") ?: "",
            isCaption2,
            emptyMap(),
            maxFileSize,
        )
    }
}
