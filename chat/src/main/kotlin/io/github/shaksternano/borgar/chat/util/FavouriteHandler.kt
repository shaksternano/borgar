package io.github.shaksternano.borgar.chat.util

import io.github.shaksternano.borgar.chat.command.FAVOURITE_ALIAS_PREFIX
import io.github.shaksternano.borgar.chat.event.MessageReceiveEvent
import io.github.shaksternano.borgar.core.io.filenameWithoutExtension
import io.github.shaksternano.borgar.core.io.removeQueryParams
import io.github.shaksternano.borgar.core.util.getUrls
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

suspend fun sendFavouriteFile(event: MessageReceiveEvent) {
    val message = event.message
    val urls = message.content.getUrls().ifEmpty { return }
    val aliasUrl = removeQueryParams(urls.first())
    val fileName = filenameWithoutExtension(aliasUrl)
    if (!fileName.startsWith(FAVOURITE_ALIAS_PREFIX)) return
    val url = getUrl(aliasUrl) ?: return
    val author = event.getAuthorMember() ?: event.getAuthor()
    val channel = event.getChannel()
    coroutineScope {
        launch {
            runCatching {
                channel.createMessage {
                    content = url
                    username = author.effectiveName
                    avatarUrl = author.effectiveAvatarUrl
                }
            }.getOrElse {
                channel.createMessage(url)
            }
        }
        launch {
            runCatching {
                event.message.delete()
            }
        }
    }
}

private fun getUrl(aliasUrl: String): String? {
    val filename = filenameWithoutExtension(aliasUrl)
    val nameParts = filename.split("_", limit = 2)
    if (nameParts.size != 2) return null
    val decodedBytes = runCatching {
        Base64.getDecoder().decode(nameParts[1])
    }.getOrElse { return null }
    return String(decodedBytes)
}
