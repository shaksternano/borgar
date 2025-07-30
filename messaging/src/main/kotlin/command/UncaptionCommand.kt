package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.UncaptionTask
import com.shakster.borgar.messaging.event.CommandEvent

object UncaptionCommand : FileCommand(
    CommandArgumentInfo(
        key = "onlycheckfirst",
        aliases = setOf("first", "f"),
        description = "Whether to only check for a caption in the first frame or not.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
) {

    override val name: String = "uncaption"
    override val description: String = "Removes the caption from media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val onlyCheckFirst = arguments.getRequired("onlycheckfirst", CommandArgumentType.Boolean)
        return UncaptionTask(onlyCheckFirst, maxFileSize)
    }
}
