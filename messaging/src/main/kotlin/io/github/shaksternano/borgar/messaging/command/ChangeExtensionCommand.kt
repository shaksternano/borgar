package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.ChangeExtensionTask
import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

class ChangeExtensionCommand(
    newExtension: String,
) : FileCommand() {

    companion object {
        val GIF: Command = ChangeExtensionCommand("gif")
    }

    override val name: String = "${newExtension}2"
    override val description: String = "Changes the extension of a file to .$newExtension."
    private val task: FileTask = ChangeExtensionTask(newExtension)

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        task
}
