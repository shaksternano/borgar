package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.collect.addAll
import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.util.asSingletonList
import com.shakster.borgar.messaging.entity.Attachment
import com.shakster.borgar.messaging.event.CommandEvent

abstract class FileCommand(
    vararg argumentInfo: CommandArgumentInfo<*>,
    private val inputRequirement: InputRequirement = InputRequirement.REQUIRED,
) : BaseCommand() {

    override val chainable: Boolean = true
    override val deferReply: Boolean = true

    private val takesInput = inputRequirement != InputRequirement.NONE
    final override val argumentInfo: Set<CommandArgumentInfo<*>> =
        if (takesInput) {
            buildSet {
                addAll(argumentInfo)
                addAll(
                    FILE_ARGUMENT_INFO,
                    URL_ARGUMENT_INFO,
                )
            }
        } else {
            argumentInfo.toSet()
        }
    override val defaultArgumentKey: String? =
        if (takesInput && this.argumentInfo.size == 2) "url"
        else super.defaultArgumentKey

    final override fun createExecutable(arguments: CommandArguments, event: CommandEvent): Executable =
        FileExecutable(
            commandConfigs = CommandConfig(this, arguments).asSingletonList(),
            arguments,
            event,
            inputRequirement,
            event.manager.maxFilesPerMessage,
        ) {
            createTask(
                arguments,
                event,
                event.getChannel().getMaxFileSize()
            )
        }

    protected abstract suspend fun createTask(
        arguments: CommandArguments,
        event: CommandEvent,
        maxFileSize: Long
    ): FileTask
}

enum class InputRequirement {
    REQUIRED,
    OPTIONAL,
    NONE,
}

val FILE_ARGUMENT_INFO: CommandArgumentInfo<Attachment> = CommandArgumentInfo(
    key = "file",
    description = "The file to use.",
    type = CommandArgumentType.Attachment,
    required = false,
)

val URL_ARGUMENT_INFO: CommandArgumentInfo<String> = CommandArgumentInfo(
    key = "url",
    description = "The URL to use.",
    type = CommandArgumentType.String,
    required = false,
)
