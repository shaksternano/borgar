package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.getContent
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.getUrlsExceptSelf
import io.github.shaksternano.borgar.core.collect.addAll
import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.UrlInfo
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.MediaProcessingTask
import io.github.shaksternano.borgar.core.io.task.TranscodeTask
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.getTenorUrlOrDefault

abstract class FileCommand(
    vararg argumentInfo: CommandArgumentInfo<*>,
    requireInput: Boolean = true,
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

    final override suspend fun run(arguments: CommandArguments, event: CommandEvent): Executable {
        val maxFileSize = event.getChannel().getMaxFileSize()
        val task = createTask(arguments, event, maxFileSize)
        var gifv = false
        val convertable = arguments.getDefaultAttachment()
            ?: getFileUrl(arguments, event, task).also {
                if (it != null)
                    gifv = it.gifv
            }
        val modifiedOutputFormatTask = if (gifv && task is MediaProcessingTask)
            task then TranscodeTask("gif", maxFileSize)
        else task
        val files = convertable?.asDataSource()?.asSingletonList() ?: emptyList()
        return FileExecutable(
            this,
            modifiedOutputFormatTask,
            files,
            maxFileSize,
            event.manager.maxFilesPerMessage,
        )
    }

    private suspend fun getFileUrl(arguments: CommandArguments, event: CommandEvent, task: FileTask): UrlInfo? {
        val messageIntersection = event.asMessageIntersection(arguments)
        val getGif = task.requireInput && task !is MediaProcessingTask
        val url = arguments.getDefaultUrl()
            ?: return messageIntersection.getUrlsExceptSelf(getGif).firstOrNull()
        val embed = messageIntersection.embeds.firstOrNull { it.url == url }
        val embedContent = embed?.getContent(getGif)
        if (embedContent != null) return embedContent
        return getTenorUrlOrDefault(url, getGif)
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
    type = CommandArgumentType.ATTACHMENT,
    required = false,
)

private val URL_ARGUMENT_INFO = CommandArgumentInfo(
    key = "url",
    description = "The URL to use.",
    type = CommandArgumentType.STRING,
    required = false,
)

private data class FileExecutable(
    override val command: Command,
    private val task: FileTask,
    private val inputs: List<DataSource>,
    private val maxFileSize: Long,
    private val maxFilesPerMessage: Int,
) : Executable {

    override suspend fun execute(): List<CommandResponse> {
        if (inputs.isEmpty()) {
            return CommandResponse("No files found!").asSingletonList()
        }
        val output = try {
            task.run(inputs)
        } catch (e: ErrorResponseException) {
            e.cause?.let { logger.error("Error executing command ${command.name}", it) }
            return CommandResponse(e.message).asSingletonList()
        }
        if (output.isEmpty()) {
            logger.error("No files were outputted by ${command.name}")
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

    override fun then(after: Executable): Executable {
        return if (after is FileExecutable) {
            copy(
                command = after.command,
                task = task then after.task,
            )
        } else {
            super.then(after)
        }
    }

    override suspend fun close() = task.close()
}
