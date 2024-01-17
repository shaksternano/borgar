package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.ChangeExtensionTask
import io.github.shaksternano.borgar.core.io.task.FileTask

class ChangeExtensionCommand(
    private val newExtension: String
) : FileCommand() {

    companion object {
        val GIF: Command = ChangeExtensionCommand("gif")
    }

    override val name: String = "${newExtension}2"
    override val description: String = "Changes the extension of a file to `.$newExtension`."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        ChangeExtensionTask(newExtension, maxFileSize)
}
