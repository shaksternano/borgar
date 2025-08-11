package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.ffmpegAvailable
import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.TranscodeTask
import com.shakster.borgar.core.util.startsWithVowel
import com.shakster.borgar.messaging.event.CommandEvent

class TranscodeCommand(
    private val format: String,
    override val register: Boolean = true,
) : FileCommand() {

    companion object {
        val PNG: Command = TranscodeCommand("png")
        val JPG: Command = TranscodeCommand("jpg")
        val MP4: Command = TranscodeCommand("mp4", ffmpegAvailable)
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
