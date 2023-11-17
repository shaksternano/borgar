package io.github.shaksternano.borgar.chat.util

import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
import io.github.shaksternano.borgar.chat.entity.CustomEmoji
import io.github.shaksternano.borgar.core.emoji.EmojiUtil
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.graphics.drawable.ImageDrawable
import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.util.getUrls
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

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

suspend fun CommandMessageIntersection.getEmojiDrawables(): Map<String, Drawable> = getEmojiUrls()
    .mapValues {
        val dataSource = DataSource.fromUrl(it.value)
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
