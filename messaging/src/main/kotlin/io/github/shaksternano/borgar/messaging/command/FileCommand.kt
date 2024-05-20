package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.collect.addAll
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.UrlInfo
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.MediaProcessingTask
import io.github.shaksternano.borgar.core.io.task.TranscodeTask
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.retrieveTenorMediaUrl
import io.github.shaksternano.borgar.messaging.entity.getContent
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import io.github.shaksternano.borgar.messaging.util.getUrlsExceptSelf

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
                    ATTACHMENT_ARGUMENT_INFO,
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

private val ATTACHMENT_ARGUMENT_INFO = CommandArgumentInfo(
    key = "attachment",
    description = "The attachment to use.",
    type = CommandArgumentType.Attachment,
    required = false,
)

private val URL_ARGUMENT_INFO = CommandArgumentInfo(
    key = "url",
    description = "The URL to use.",
    type = CommandArgumentType.String,
    required = false,
)

private data class FileExecutable(
    override val commandConfigs: List<CommandConfig>,
    private val arguments: CommandArguments,
    private val event: CommandEvent,
    private val inputRequirement: InputRequirement,
    private val maxFilesPerMessage: Int,
    private val taskSupplier: suspend () -> FileTask,
) : Executable {

    private var toClose: SuspendCloseable? = null

    override suspend fun run(): List<CommandResponse> {
        val maxFileSize = event.getChannel().getMaxFileSize()
        val task = taskSupplier()
        toClose = task
        var gifv = false
        val input =
            if (inputRequirement != InputRequirement.NONE) {
                val file = task.suppliedInput
                    ?: getFileUrl(arguments, event, task)
                        ?.also {
                            gifv = it.gifv
                        }
                        ?.asDataSource()
                if (inputRequirement == InputRequirement.REQUIRED && file == null) {
                    return CommandResponse("No input found!").asSingletonList()
                }
                listOfNotNull(file)
            } else {
                emptyList()
            }

        val modifiedOutputFormatTask = if (gifv && task is MediaProcessingTask)
            task then TranscodeTask("gif", maxFileSize)
        else task
        val output = modifiedOutputFormatTask.run(input)
        if (output.isEmpty()) {
            logger.error("No files were outputted by ${commandConfigs.last().typedForm}")
            return CommandResponse("An error occurred!").asSingletonList()
        }
        val canUpload = output.filter {
            it.sendUrl || it.isWithinReportedSize(maxFileSize)
        }
        return canUpload.chunked(maxFilesPerMessage).mapIndexed { index, files ->
            val partitioned = files.partition {
                it.sendUrl && it.url != null
            }
            val urls = partitioned.first.joinToString("\n") { it.url!! }
            val attachments = partitioned.second
            CommandResponse(
                content = when {
                    index == 0 && canUpload.size < output.size -> "Some files are too large to upload."
                    else -> urls
                },
                files = attachments,
            )
        }.ifEmpty {
            val message = if (output.size == 1)
                "File is too large to upload."
            else "Files are too large to upload."
            CommandResponse(message).asSingletonList()
        }
    }

    private suspend fun getFileUrl(arguments: CommandArguments, event: CommandEvent, task: FileTask): UrlInfo? {
        arguments.getDefaultAttachment()?.let {
            return UrlInfo(
                it.url,
                it.filename,
                false,
            )
        }
        val messageIntersection = event.asMessageIntersection(arguments)
        val getGif = task.requireInput && task !is MediaProcessingTask
        val url = arguments.getDefaultUrl()
            ?: return messageIntersection.getUrlsExceptSelf(getGif).firstOrNull()
        val embed = messageIntersection.getEmbeds().firstOrNull { it.url == url }
        val embedContent = embed?.getContent(getGif)
        if (embedContent != null) return embedContent
        return retrieveTenorMediaUrl(url, getGif) ?: UrlInfo(url)
    }

    override fun then(after: Executable): Executable {
        return if (after is FileExecutable) {
            copy(
                commandConfigs = commandConfigs + after.commandConfigs,
                taskSupplier = {
                    try {
                        taskSupplier() then after.taskSupplier()
                    } catch (e: UnsupportedOperationException) {
                        throw NonChainableCommandException(commandConfigs.last(), after.commandConfigs.first())
                    }
                },
            )
        } else {
            super.then(after)
        }
    }

    override suspend fun close() {
        toClose?.close()
    }
}
