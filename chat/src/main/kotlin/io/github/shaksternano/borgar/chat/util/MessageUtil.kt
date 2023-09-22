package io.github.shaksternano.borgar.chat.util

import io.github.shaksternano.borgar.chat.command.CommandMessageUnion
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.DataSourceConvertable
import io.github.shaksternano.borgar.core.io.UrlDataSource
import io.github.shaksternano.borgar.core.io.filename
import io.github.shaksternano.borgar.core.util.getUrls
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private const val MAX_PAST_MESSAGES_TO_CHECK = 50

suspend fun CommandMessageUnion.getUrls(): List<UrlInfo> {
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

suspend fun <T> CommandMessageUnion.search(find: suspend (CommandMessageUnion) -> T?): T? {
    return searchVisitors(
        find,
        CommandMessageUnion::searchReferencedMessage,
        CommandMessageUnion::searchSelf,
        CommandMessageUnion::searchPreviousMessages,
    )
}

@Suppress("SameParameterValue")
private suspend fun <T> CommandMessageUnion.searchVisitors(
    find: suspend (CommandMessageUnion) -> T?,
    vararg messageVisitors: suspend CommandMessageUnion.(suspend (CommandMessageUnion) -> T?) -> T?,
): T? = messageVisitors.firstNotNullOfOrNull {
    it(find)
}

private suspend fun <T> CommandMessageUnion.searchReferencedMessage(find: suspend (CommandMessageUnion) -> T?): T? {
    return getReferencedMessage()?.let {
        find(it.asCommandUnion())
    }
}

private suspend fun <T> CommandMessageUnion.searchSelf(find: suspend (CommandMessageUnion) -> T?): T? {
    return find(this)
}

private suspend fun <T> CommandMessageUnion.searchPreviousMessages(find: suspend (CommandMessageUnion) -> T?): T? {
    val previousMessages = getPreviousMessages(MAX_PAST_MESSAGES_TO_CHECK)
    return previousMessages.map {
        find(it.asCommandUnion())
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
