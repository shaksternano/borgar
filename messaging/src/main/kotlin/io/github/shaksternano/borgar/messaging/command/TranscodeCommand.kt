package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.TranscodeTask
import io.github.shaksternano.borgar.core.util.startsWithVowel
import io.github.shaksternano.borgar.messaging.event.CommandEvent

class TranscodeCommand(
    private val format: String,
) : FileCommand() {

    companion object {
        val PNG: Command = TranscodeCommand("png")
        val JPG: Command = TranscodeCommand("jpg")
        val GIF: Command = TranscodeCommand("gif")
        val MP4: Command = TranscodeCommand("mp4")
        val ICO: Command = TranscodeCommand("ico")
    }

    override val name: String = format
    override val description: String = run {
        var transcodeDescription = "Converts media to a"
        if (format.startsWithVowel()) {
            transcodeDescription += "n"
        }
        transcodeDescription + " ${format.uppercase()} file."
    }

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        TranscodeTask(format, maxFileSize)
}
