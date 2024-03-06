package io.github.shaksternano.borgar.chat.util

import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
import io.github.shaksternano.borgar.chat.entity.getContent
import io.github.shaksternano.borgar.core.emoji.getEmojiUrl
import io.github.shaksternano.borgar.core.emoji.getEmojiUrlFromShortcode
import io.github.shaksternano.borgar.core.emoji.isEmojiUnicode
import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.graphics.drawable.ImageDrawable
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.UrlInfo
import io.github.shaksternano.borgar.core.util.TenorMediaType
import io.github.shaksternano.borgar.core.util.getUrls
import io.github.shaksternano.borgar.core.util.retrieveTenorMediaUrl
import io.github.shaksternano.borgar.core.util.retrieveTenorUrlOrDefault
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toSet
import kotlin.math.min

private const val MAX_PAST_MESSAGES_TO_CHECK: Int = 100

suspend fun CommandMessageIntersection.getUrlsExceptSelf(getGif: Boolean): List<UrlInfo> =
    searchExceptSelf {
        val urls = it.getUrls(getGif)
        urls.ifEmpty {
            null
        }
    } ?: emptyList()

suspend fun CommandMessageIntersection.getUrls(getGif: Boolean): List<UrlInfo> = buildList {
    addAll(
        attachments.map { attachment ->
            UrlInfo(attachment.url, attachment.filename)
        }
    )
    addAll(
        embeds.mapNotNull {
            it.getContent(getGif)
        }
    )
    addAll(
        content.getUrls().map {
            retrieveTenorUrlOrDefault(it, getGif)
        }
    )
}

suspend fun CommandMessageIntersection.getEmojiAndUrlDrawables(): Map<String, Drawable> =
    getEmojiDrawables() + getUrlDrawables()

private suspend fun CommandMessageIntersection.getEmojiDrawables(): Map<String, Drawable> =
    getEmojiUrls().mapValues {
        val dataSource = DataSource.fromUrl(url = it.value)
        ImageDrawable(dataSource)
    }

private suspend fun CommandMessageIntersection.getUrlDrawables(): Map<String, Drawable> {
    val embeds = embeds.associateBy { it.url }
    return content.getUrls()
        .associateBy { it }
        .mapNotNull { entry ->
            val url = embeds[entry.key]?.getContent(false)?.url
                ?: retrieveTenorMediaUrl(entry.key, TenorMediaType.MP4_NORMAL)
                ?: entry.key
            val dataSource = DataSource.fromUrl(url)
            runCatching {
                ImageDrawable(dataSource)
            }.map { entry.key to it }.getOrNull()
        }.associate { it }
}

suspend fun CommandMessageIntersection.getEmojiUrls(): Map<String, String> {
    val emojiUrls = mutableMapOf<String, String>()

    // Get custom emojis.
    customEmojis.collect {
        emojiUrls[it.asMention] = it.imageUrl
    }

    // Get undetected emojis, such as those requiring Discord nitro.
    getGuild()?.let { guild ->
        val existingEmojis = customEmojis.map { it.name }.toSet()
        guild.getCustomEmojis().forEach {
            if (!existingEmojis.contains(it.name)) {
                val basicMention = it.asBasicMention
                if (content.contains(basicMention)) {
                    emojiUrls[basicMention] = it.imageUrl
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

            if (isEmojiUnicode(compositeUnicodeBuilder.toString())) {
                val emojiCharactersBuilder = StringBuilder()
                compositeCodePoints.forEach(emojiCharactersBuilder::appendCodePoint)
                emojiUrls[emojiCharactersBuilder.toString()] = getEmojiUrl(compositeUnicodeBuilder.toString())
                i += j - i
                break
            }
        }
        i++
    }

    // Get unicode emojis from shortcodes.
    manager.emojiTypedPattern.findAll(content).forEach {
        val emojiName = manager.getEmojiName(it.value)
        val emojiUrl = getEmojiUrlFromShortcode(emojiName)
        if (emojiUrl != null) {
            emojiUrls[emojiName] = emojiUrl
        }
    }

    return emojiUrls
}

suspend fun <T> CommandMessageIntersection.search(find: suspend (CommandMessageIntersection) -> T?): T? =
    searchVisitors(
        find,
        CommandMessageIntersection::searchSelf,
        CommandMessageIntersection::searchReferencedMessages,
        CommandMessageIntersection::searchPreviousMessages,
    )

suspend fun <T> CommandMessageIntersection.searchExceptSelf(find: suspend (CommandMessageIntersection) -> T?): T? =
    searchVisitors(
        find,
        CommandMessageIntersection::searchReferencedMessages,
        CommandMessageIntersection::searchPreviousMessages,
    )

suspend fun <T> CommandMessageIntersection.searchOrThrow(
    errorMessage: String,
    find: suspend (CommandMessageIntersection) -> T?,
): T = search(find) ?: throw ErrorResponseException(errorMessage)

suspend fun <T> CommandMessageIntersection.searchExceptSelfOrThrow(
    errorMessage: String,
    find: suspend (CommandMessageIntersection) -> T?,
): T = searchExceptSelf(find) ?: throw ErrorResponseException(errorMessage)

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
