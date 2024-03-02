package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.getEmojiAndUrlDrawables
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.TemplateTask
import io.github.shaksternano.borgar.core.media.template.CustomTemplate
import io.github.shaksternano.borgar.core.media.template.ResourceTemplate
import io.github.shaksternano.borgar.core.media.template.Template

class TemplateCommand(
    override val name: String,
    override val aliases: Set<String> = emptySet(),
    override val description: String,
    private val template: Template,
    override val entityId: String? = null,
) : FileCommand(
    CommandArgumentInfo(
        key = "text",
        description = "The text to put on the image",
        type = CommandArgumentType.String,
        required = false,
    ),
    inputRequirement = InputRequirement.Optional,
) {

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

        val MUTA_SOY: Command = TemplateCommand(
            name = "mutasoy",
            description = "Mutahar soyjak pointing meme.",
            template = ResourceTemplate.MUTA_SOY,
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

    constructor(customTemplate: CustomTemplate) : this(
        name = customTemplate.commandName,
        description = customTemplate.description,
        template = customTemplate,
        entityId = customTemplate.entityId
    )

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val text = arguments.getOptional("text", CommandArgumentType.String)
        val nonTextParts =
            if (text.isNullOrBlank()) emptyMap()
            else event.asMessageIntersection(arguments).getEmojiAndUrlDrawables()
        return TemplateTask(template, text, nonTextParts, maxFileSize)
    }
}
