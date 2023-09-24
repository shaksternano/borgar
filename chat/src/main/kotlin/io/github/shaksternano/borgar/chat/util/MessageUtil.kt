package io.github.shaksternano.borgar.chat.util

import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.DataSourceConvertable
import io.github.shaksternano.borgar.core.io.UrlDataSource
import io.github.shaksternano.borgar.core.io.filename
import io.github.shaksternano.borgar.core.util.getUrls
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private const val MAX_PAST_MESSAGES_TO_CHECK = 50

suspend fun CommandMessageIntersection.getUrls(): List<UrlInfo> {
    return search {
        val urls = buildList {
            addAll(
                it.attachments.map { attachment ->
                    UrlInfo(attachment.url, attachment.fileName)
                }
            )
            addAll(
                it.content.getUrls().map {
                    UrlInfo(it)
                }
            )
            addAll(
                it.embeds.mapNotNull {
                    val url = it.image?.url ?: it.video?.url
                    url?.let(::UrlInfo)
                }
            )
        }
        urls.ifEmpty {
            null
        }
    } ?: emptyList()
}

suspend fun <T> CommandMessageIntersection.search(find: suspend (CommandMessageIntersection) -> T?): T? {
    return searchVisitors(
        find,
        CommandMessageIntersection::searchReferencedMessage,
        CommandMessageIntersection::searchSelf,
        CommandMessageIntersection::searchPreviousMessages,
    )
}

@Suppress("SameParameterValue")
private suspend fun <T> CommandMessageIntersection.searchVisitors(
    find: suspend (CommandMessageIntersection) -> T?,
    vararg messageVisitors: suspend CommandMessageIntersection.(suspend (CommandMessageIntersection) -> T?) -> T?,
): T? = messageVisitors.firstNotNullOfOrNull {
    it(find)
}

private suspend fun <T> CommandMessageIntersection.searchReferencedMessage(find: suspend (CommandMessageIntersection) -> T?): T? {
    return getReferencedMessage()?.let {
        find(it.asCommandIntersection())
    }
}

private suspend fun <T> CommandMessageIntersection.searchSelf(find: suspend (CommandMessageIntersection) -> T?): T? {
    return find(this)
}

private suspend fun <T> CommandMessageIntersection.searchPreviousMessages(find: suspend (CommandMessageIntersection) -> T?): T? {
    val previousMessages = getPreviousMessages(MAX_PAST_MESSAGES_TO_CHECK)
    return previousMessages.map {
        find(it.asCommandIntersection())
    }.firstOrNull {
        it != null
    }
}

data class UrlInfo(
    val url: String,
    val fileName: String,
) : DataSourceConvertable {

    constructor(url: String) : this(url, filename(url))

    override fun asDataSource(): UrlDataSource = DataSource.fromUrl(url, fileName)
}
