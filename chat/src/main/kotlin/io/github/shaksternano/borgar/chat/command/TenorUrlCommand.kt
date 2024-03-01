package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.searchExceptSelfOrThrow
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask
import io.github.shaksternano.borgar.core.util.TenorMediaType
import io.github.shaksternano.borgar.core.util.getUrls
import io.github.shaksternano.borgar.core.util.isTenorUrl
import io.github.shaksternano.borgar.core.util.retrieveTenorMediaUrl

private val TENOR_MEDIA_TYPE: CommandArgumentType<TenorMediaType> =
    CommandArgumentType.Enum(TenorMediaType::class, "Tenor media type")

object TenorUrlCommand : FileCommand(
    CommandArgumentInfo(
        key = "mediatype",
        aliases = setOf("type"),
        description = "The type of media to get the URL of",
        type = TENOR_MEDIA_TYPE,
        required = false,
        defaultValue = TenorMediaType.GIF_LARGE,
    ),
    CommandArgumentInfo(
        key = "url",
        description = "The tenor URL to use.",
        type = CommandArgumentType.String,
        required = false,
        validator = TenorUrlValidator,
    ),
    inputRequirement = InputRequirement.None,
) {

    override val name: String = "tenorurl"
    override val aliases: Set<String> = setOf("tenor")
    override val description: String = "Gets the direct file URL of Tenor media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val mediaType = arguments.getRequired("mediatype", TENOR_MEDIA_TYPE)
        val url = arguments.getOptional("url", CommandArgumentType.String)
            ?: event.asMessageIntersection(arguments)
                .searchExceptSelfOrThrow("No Tenor URLs found.") {
                    val url = it.content.getUrls().firstOrNull() ?: ""
                    if (isTenorUrl(url)) url
                    else null
                }
        val tenorMediaUrl = retrieveTenorMediaUrl(url, mediaType)
            ?: throw IllegalStateException("No media found for url $url with media type $mediaType.")
        return UrlFileTask(tenorMediaUrl)
    }
}

private object TenorUrlValidator : Validator<String> {

    override fun validate(value: String): Boolean =
        isTenorUrl(value)

    override fun errorMessage(value: String, key: String): String =
        "The argument **$key** must be a Tenor URL."
}
