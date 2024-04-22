package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.io.task.ChangeExtensionTask
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

class ChangeExtensionCommand(
    private val newExtension: String
) : FileCommand() {

    companion object {
        val GIF: Command = ChangeExtensionCommand("gif")
    }

    override val name: String = "${newExtension}2"
    override val description: String = "Changes the extension of a file to .$newExtension."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        ChangeExtensionTask(newExtension)
}
