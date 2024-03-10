package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.SpeechBubbleTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

val SPEECH_BUBBLE_FLIP_ARGUMENT: CommandArgumentInfo<*> = CommandArgumentInfo(
    key = "flip",
    aliases = setOf("f"),
    description = "Whether to flip the speech bubble or not.",
    type = CommandArgumentType.Boolean,
    required = false,
    defaultValue = false,
)

object SpeechBubbleCommand : FileCommand(
    SPEECH_BUBBLE_FLIP_ARGUMENT,
) {

    override val name: String = "speechbubble"
    override val aliases: Set<String> = setOf("sb")
    override val description: String = "Overlays a speech bubble over media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val flipped = arguments.getRequired("flip", CommandArgumentType.Boolean)
        return SpeechBubbleTask(
            cutout = false,
            flipped = flipped,
            opaque = false,
            maxFileSize = maxFileSize,
        )
    }
}
