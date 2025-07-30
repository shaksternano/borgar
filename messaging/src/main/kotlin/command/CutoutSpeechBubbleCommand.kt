package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.SpeechBubbleTask
import com.shakster.borgar.messaging.event.CommandEvent

object CutoutSpeechBubbleCommand : FileCommand(
    SPEECH_BUBBLE_FLIP_ARGUMENT,
    CommandArgumentInfo(
        key = "opaque",
        aliases = setOf("o"),
        description = "Whether to make the speech bubble opaque or not.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
) {

    override val name: String = "cutoutspeechbubble"
    override val aliases: Set<String> = setOf("sbi")
    override val description: String = "Cuts out a speech bubble from media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val flipped = arguments.getRequired("flip", CommandArgumentType.Boolean)
        val opaque = arguments.getRequired("opaque", CommandArgumentType.Boolean)
        return SpeechBubbleTask(
            cutout = true,
            flipped = flipped,
            opaque = opaque,
            maxFileSize = maxFileSize,
        )
    }
}
