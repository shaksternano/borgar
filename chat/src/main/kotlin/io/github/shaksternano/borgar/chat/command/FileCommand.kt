package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.UrlInfo
import io.github.shaksternano.borgar.chat.util.getUrls
import io.github.shaksternano.borgar.core.collect.addAll
import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.filename
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.TenorMediaType
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.retrieveTenorMediaUrl

abstract class FileCommand(
    vararg argumentInfo: CommandArgumentInfo<*>,
    requireInput: Boolean = true,
) : BaseCommand() {

    final override val argumentInfo: Set<CommandArgumentInfo<*>> = if (requireInput) {
        val argumentInfoBuilder = mutableSetOf<CommandArgumentInfo<*>>()
        argumentInfoBuilder.addAll(argumentInfo)
        argumentInfoBuilder.addAll(
            CommandArgumentInfo(
                key = "attachment",
                description = "The attachment to use",
                type = SimpleCommandArgumentType.ATTACHMENT,
                required = false,
            ),
            CommandArgumentInfo(
                key = "url",
                description = "The URL to use",
                type = SimpleCommandArgumentType.STRING,
                required = false,
            ),
        )
        argumentInfoBuilder
    } else {
        argumentInfo.toSet()
    }

    final override suspend fun run(arguments: CommandArguments, event: CommandEvent): Executable {
        val convertable = arguments.getDefaultAttachment() ?: run {
            val urlInfo = arguments.getDefaultUrl()?.let {
                UrlInfo(it, filename(it))
            } ?: event.asMessageIntersection(arguments).getUrls().firstOrNull()
            val tenorUrl = urlInfo?.let { retrieveTenorMediaUrl(it.url, TenorMediaType.GIF_NORMAL) }
            tenorUrl?.let { UrlInfo(it, filename(it)) } ?: urlInfo
        }
        val files = convertable?.asDataSource()?.asSingletonList() ?: emptyList()
        val maxFileSize = event.getChannel().getMaxFileSize()
        return FileExecutable(
            this,
            createTask(arguments, event, maxFileSize),
            files,
            maxFileSize,
            event.manager.maxFilesPerMessage,
        )
    }

    protected abstract suspend fun createTask(
        arguments: CommandArguments,
        event: CommandEvent,
        maxFileSize: Long
    ): FileTask
}

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

    override suspend fun cleanup() = task.cleanup()

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
}
