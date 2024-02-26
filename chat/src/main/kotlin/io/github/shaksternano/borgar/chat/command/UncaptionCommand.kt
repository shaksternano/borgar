package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UncaptionTask

sealed class UncaptionCommand(
    override val name: String,
    override val description: String,
    private val coloredCaption: Boolean,
) : FileCommand(
    CommandArgumentInfo(
        key = "onlycheckfirst",
        aliases = setOf("first", "f"),
        description = "Whether to only check for a caption in the first frame or not.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
) {

    object Uncaption : UncaptionCommand(
        "uncaption",
        "Uncaptions media that has color in the caption.",
        true,
    )

    object Uncaption2 : UncaptionCommand(
        "uncaption2",
        "Uncaptions media that doesn't have color in the caption.",
        false,
    )

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val onlyCheckFirst = arguments.getRequired("onlycheckfirst", CommandArgumentType.Boolean)
        return UncaptionTask(coloredCaption, onlyCheckFirst, maxFileSize)
    }
}
