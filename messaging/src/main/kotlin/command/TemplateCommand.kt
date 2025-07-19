package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.media.template.CustomTemplate
import io.github.shaksternano.borgar.core.media.template.ResourceTemplate
import io.github.shaksternano.borgar.core.media.template.Template
import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.TemplateTask
import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import io.github.shaksternano.borgar.messaging.util.getEmojiAndUrlDrawables

class TemplateCommand(
    override val name: String,
    override val aliases: Set<String> = emptySet(),
    override val description: String,
    private val template: Template,
    override val entityId: String? = null,
    override val entityEnvironment: ChannelEnvironment? = null,
) : FileCommand(
    CommandArgumentInfo(
        key = "text",
        description = "The text to put on the image",
        type = CommandArgumentType.String,
        required = false,
    ),
    inputRequirement = InputRequirement.OPTIONAL,
) {

    constructor(customTemplate: CustomTemplate) : this(
        name = customTemplate.commandName,
        description = customTemplate.description,
        template = customTemplate,
        entityId = customTemplate.entityId,
        entityEnvironment = customTemplate.entityEnvironment,
    )

    companion object {
        val SONIC_SAYS: Command = TemplateCommand(
            name = "sonicsays",
            aliases = setOf("sonic"),
            description = "Sonic says meme.",
            template = ResourceTemplate.SONIC_SAYS,
        )

        val SOYJAK_POINTING: Command = TemplateCommand(
            name = "soyjakpointing",
            aliases = setOf("soyjak", "soy"),
            description = "Soyjak pointing meme.",
            template = ResourceTemplate.SOYJAK_POINTING,
        )

        val WALMART_WANTED: Command = TemplateCommand(
            name = "walmartwanted",
            aliases = setOf("wanted"),
            description = "Walmart wanted meme.",
            template = ResourceTemplate.WALMART_WANTED,
        )

        val OH_MY_GOODNESS_GRACIOUS: Command = TemplateCommand(
            name = "ohmygoodnessgracious",
            aliases = setOf("omgg"),
            description = "Oh my goodness gracious meme.",
            template = ResourceTemplate.OH_MY_GOODNESS_GRACIOUS,
        )

        val THINKING_BUBBLE: Command = TemplateCommand(
            name = "thinkingbubble",
            aliases = setOf("thinking", "think"),
            description = "Thinking bubble meme.",
            template = ResourceTemplate.THINKING_BUBBLE,
        )

        val LIVING_IN_1984: Command = TemplateCommand(
            name = "1984",
            description = "Living in 1984 meme.",
            template = ResourceTemplate.LIVING_IN_1984,
        )

        val WHO_DID_THIS: Command = TemplateCommand(
            name = "whodidthis",
            aliases = setOf("wdt"),
            description = "Who did this meme.",
            template = ResourceTemplate.WHO_DID_THIS,
        )
    }

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val text = arguments.getOptional("text", CommandArgumentType.String)
        val messageIntersection = event.asMessageIntersection(arguments)
        return TemplateTask(
            template = template,
            text = text?.let { formatMentions(it, messageIntersection) },
            nonTextParts = messageIntersection.getEmojiAndUrlDrawables(),
            maxFileSize = maxFileSize,
        )
    }
}
