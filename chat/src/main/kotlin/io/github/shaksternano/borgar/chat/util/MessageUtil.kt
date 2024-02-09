package io.github.shaksternano.borgar.chat.util

import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
import io.github.shaksternano.borgar.chat.entity.CustomEmoji
import io.github.shaksternano.borgar.core.emoji.EmojiUtil
import io.github.shaksternano.borgar.core.exception.FailedOperationException
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.graphics.drawable.ImageDrawable
import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.util.getUrls
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

private const val MAX_PAST_MESSAGES_TO_CHECK: Int = 100

suspend fun CommandMessageIntersection.getUrls(): List<UrlInfo> =
    search {
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

suspend fun CommandMessageIntersection.getEmojiDrawables(): Map<String, Drawable> = getEmojiUrls()
    .mapValues {
        val dataSource = DataSource.fromUrl(url = it.value)
        ImageDrawable(dataSource, dataSource.fileFormat())
    }

suspend fun CommandMessageIntersection.getEmojiUrls(): Map<String, String> =
    getEmojiUrls(false)

suspend fun CommandMessageIntersection.getFirstEmojiUrl(): String? =
    getEmojiUrls(true).values.firstOrNull()

private suspend fun CommandMessageIntersection.getEmojiUrls(onlyGetFirst: Boolean): Map<String, String> {
    val emojiUrls = mutableMapOf<String, String>()

    // Get custom emojis.
    customEmojis.forEach {
        emojiUrls[it.asMention] = it.imageUrl
        if (onlyGetFirst) {
            return emojiUrls
        }
    }

    // Get undetected emojis, such as those requiring Discord nitro.
    getGuild()?.let { guild ->
        val existingEmojis = customEmojis.map(CustomEmoji::name).toSet()
        guild.getCustomEmojis().forEach {
            if (!existingEmojis.contains(it.name)) {
                val basicMention = it.asBasicMention
                if (content.contains(basicMention)) {
                    emojiUrls[basicMention] = it.imageUrl
                    if (onlyGetFirst) {
                        return emojiUrls
                    }
                }
            }
        }
    }

    // Get unicode emojis.
    val codePoints = content.codePoints().toArray()
    var i = 0
    while (i < codePoints.size) {
        for (j in min(codePoints.size - 1, 10 + i) downTo i) {
            val compositeCodePoints = mutableListOf<Int>()
            val compositeUnicodeBuilder = StringBuilder()
            for (k in i..j) {
                val codePoint = codePoints[k]
                val hexCodePoint = codePoint.toString(16)
                compositeCodePoints.add(codePoint)
                compositeUnicodeBuilder.append(hexCodePoint).append("-")
            }
            compositeUnicodeBuilder.deleteCharAt(compositeUnicodeBuilder.length - 1)

            if (EmojiUtil.isEmojiUnicode(compositeUnicodeBuilder.toString())) {
                val emojiCharactersBuilder = StringBuilder()
                compositeCodePoints.forEach(emojiCharactersBuilder::appendCodePoint)
                emojiUrls[emojiCharactersBuilder.toString()] =
                    EmojiUtil.getEmojiUrl(compositeUnicodeBuilder.toString())
                return if (onlyGetFirst) {
                    emojiUrls
                } else {
                    i += j - i
                    break
                }
            }
        }
        i++
    }

    // Get unicode emojis from shortcodes.
    manager.emojiTypedPattern.findAll(content).forEach {
        val emojiName = manager.getEmojiName(it.value)
        EmojiUtil.getEmojiUrlFromShortcode(emojiName).getOrNull()?.let { emojiUrl ->
            emojiUrls[emojiName] = emojiUrl
            if (onlyGetFirst) {
                return emojiUrls
            }
        }
    }

    return emojiUrls
}

suspend fun <T> CommandMessageIntersection.search(find: suspend (CommandMessageIntersection) -> T?): T? =
    searchVisitors(
        find,
        CommandMessageIntersection::searchReferencedMessages,
        CommandMessageIntersection::searchSelf,
        CommandMessageIntersection::searchPreviousMessages,
    )

suspend fun <T> CommandMessageIntersection.searchOrThrow(
    errorMessage: String,
    find: suspend (CommandMessageIntersection) -> T?,
): T = search(find) ?: throw FailedOperationException(errorMessage)

private suspend fun <T> CommandMessageIntersection.searchVisitors(
    find: suspend (CommandMessageIntersection) -> T?,
    vararg messageVisitors: suspend CommandMessageIntersection.(suspend (CommandMessageIntersection) -> T?) -> T?,
): T? = messageVisitors.firstNotNullOfOrNull {
    it(find)
}

private suspend fun <T> CommandMessageIntersection.searchReferencedMessages(find: suspend (CommandMessageIntersection) -> T?): T? =
    referencedMessages
        .map {
            find(it)
        }
        .firstOrNull {
            it != null
        }

private suspend fun <T> CommandMessageIntersection.searchSelf(find: suspend (CommandMessageIntersection) -> T?): T? =
    find(this)

private suspend fun <T> CommandMessageIntersection.searchPreviousMessages(find: suspend (CommandMessageIntersection) -> T?): T? =
    getPreviousMessages()
        .take(MAX_PAST_MESSAGES_TO_CHECK)
        .map {
            find(it)
        }
        .firstOrNull {
            it != null
        }

data class UrlInfo(
    val url: String,
    val filename: String,
) : DataSourceConvertable {

    constructor(url: String) : this(url, filename(url))

    override fun asDataSource(): UrlDataSource = DataSource.fromUrl(filename, url)
}
