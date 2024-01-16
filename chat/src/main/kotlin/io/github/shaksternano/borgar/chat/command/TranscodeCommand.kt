package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.TranscodeTask
import io.github.shaksternano.borgar.core.util.VOWELS

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
        val firstCharacter = format.getOrElse(0) { ' ' }.lowercaseChar()
        if (firstCharacter in VOWELS) {
            transcodeDescription += "n"
        }
        transcodeDescription + " ${format.uppercase()} file."
    }

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        TranscodeTask(format, maxFileSize)
}
