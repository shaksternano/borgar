package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.UrlFileTask
import com.shakster.borgar.core.util.TenorMediaType
import com.shakster.borgar.core.util.getUrls
import com.shakster.borgar.core.util.isTenorUrl
import com.shakster.borgar.core.util.retrieveTenorMediaUrl
import com.shakster.borgar.messaging.event.CommandEvent
import com.shakster.borgar.messaging.util.searchExceptSelfOrThrow

private val TENOR_MEDIA_TYPE: CommandArgumentType<TenorMediaType> =
    CommandArgumentType.Enum<TenorMediaType>("Tenor media type")

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
    inputRequirement = InputRequirement.NONE,
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
