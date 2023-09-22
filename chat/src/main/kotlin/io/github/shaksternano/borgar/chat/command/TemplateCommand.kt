package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.media.template.CustomTemplate
import io.github.shaksternano.borgar.core.media.template.Template

class TemplateCommand(
    override val name: String,
    override val description: String,
    private val template: Template,
) : FileCommand() {

    constructor(customTemplate: CustomTemplate) : this(
        customTemplate.commandName,
        customTemplate.description,
        customTemplate
    )

    override fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        TODO("Not yet implemented")
    }
}
