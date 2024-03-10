package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.SpeechBubbleTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

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
