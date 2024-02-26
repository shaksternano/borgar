package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.getContent
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.getUrlsExceptSelf
import io.github.shaksternano.borgar.core.collect.addAll
import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.UrlInfo
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.MediaProcessingTask
import io.github.shaksternano.borgar.core.io.task.TranscodeTask
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.retrieveTenorUrlOrDefault

abstract class FileCommand(
    vararg argumentInfo: CommandArgumentInfo<*>,
    private val requireInput: Boolean = true,
) : BaseCommand() {

    override val chainable: Boolean = true
    override val deferReply: Boolean = true

    final override val argumentInfo: Set<CommandArgumentInfo<*>> = if (requireInput) {
        val argumentInfoBuilder = mutableSetOf<CommandArgumentInfo<*>>()
        argumentInfoBuilder.addAll(argumentInfo)
        argumentInfoBuilder.addAll(
            ATTACHMENT_ARGUMENT_INFO,
            URL_ARGUMENT_INFO,
        )
        argumentInfoBuilder
    } else {
        argumentInfo.toSet()
    }
    override val defaultArgumentKey: String? =
        if (requireInput && this.argumentInfo.size == 2) "url"
        else super.defaultArgumentKey

    final override fun run(arguments: CommandArguments, event: CommandEvent): Executable =
        FileExecutable(
            commands = listOf(this),
            arguments,
            event,
            requireInput,
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
    override val commands: List<Command>,
    private val arguments: CommandArguments,
    private val event: CommandEvent,
    private val requireInput: Boolean,
    private val maxFilesPerMessage: Int,
    private val taskSupplier: suspend () -> FileTask,
) : Executable {

    private var toClose: SuspendCloseable? = null

    override suspend fun execute(): List<CommandResponse> {
        val maxFileSize = event.getChannel().getMaxFileSize()
        val task = taskSupplier()
        toClose = task
        var gifv = false
        val input = if (requireInput) {
            getFileUrl(arguments, event, task)
                ?.also {
                    gifv = it.gifv
                }
                ?.asDataSource()
                ?.asSingletonList()
                ?: return CommandResponse("No files found!").asSingletonList()
        } else {
            emptyList()
        }

        val modifiedOutputFormatTask = if (gifv && task is MediaProcessingTask)
            task then TranscodeTask("gif", maxFileSize)
        else task
        val output = try {
            modifiedOutputFormatTask.run(input)
        } catch (e: ErrorResponseException) {
            e.cause?.let { throwable ->
                logger.error(
                    "Error executing command ${
                        commands.joinToString(", ") {
                            it.name
                        }
                    }", throwable
                )
            }
            return CommandResponse(e.message).asSingletonList()
        }

        if (output.isEmpty()) {
            logger.error("No files were outputted by ${commands.last().name}")
            return CommandResponse("An error occurred!").asSingletonList()
        }
        val canUpload = output.filter {
            it.sendUrl || it.size() <= maxFileSize
        }
        return canUpload.chunked(maxFilesPerMessage).mapIndexed { index, files ->
            val partitioned = files.partition {
                it.sendUrl && it.url != null
            }
            val urls = partitioned.first.joinToString("\n") { it.url!! }
            val attachments = partitioned.second
            CommandResponse(
                content = when {
                    index == 0 && canUpload.isEmpty() -> "Files are too large to upload."
                    index == 0 && canUpload.size < output.size -> "Some files are too large to upload."
                    else -> urls
                },
                files = attachments,
            )
        }
    }

    private suspend fun getFileUrl(arguments: CommandArguments, event: CommandEvent, task: FileTask): UrlInfo? {
        arguments.getDefaultAttachment()?.let {
            return UrlInfo(
                it.proxyUrl,
                it.filename,
                false,
            )
        }
        val messageIntersection = event.asMessageIntersection(arguments)
        val getGif = task.requireInput && task !is MediaProcessingTask
        val url = arguments.getDefaultUrl()
            ?: return messageIntersection.getUrlsExceptSelf(getGif).firstOrNull()
        val embed = messageIntersection.embeds.firstOrNull { it.url == url }
        val embedContent = embed?.getContent(getGif)
        if (embedContent != null) return embedContent
        return retrieveTenorUrlOrDefault(url, getGif)
    }

    override fun then(after: Executable): Executable {
        return if (after is FileExecutable) {
            copy(
                commands = commands + after.commands,
                taskSupplier = { taskSupplier() then after.taskSupplier() },
            )
        } else {
            super.then(after)
        }
    }

    override suspend fun close() {
        toClose?.close()
    }
}
