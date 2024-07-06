package io.github.shaksternano.borgar.messaging.util

import io.github.shaksternano.borgar.core.emoji.getEmojiUrl
import io.github.shaksternano.borgar.core.emoji.getEmojiUrlFromShortcode
import io.github.shaksternano.borgar.core.emoji.isEmojiUnicode
import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.graphics.drawable.ImageDrawable
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.UrlInfo
import io.github.shaksternano.borgar.core.io.useHttpClient
import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.core.util.TenorMediaType
import io.github.shaksternano.borgar.core.util.getUrls
import io.github.shaksternano.borgar.core.util.retrieveTenorMediaUrl
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.MessagingPlatform
import io.github.shaksternano.borgar.messaging.command.CommandMessageIntersection
import io.github.shaksternano.borgar.messaging.entity.Message
import io.github.shaksternano.borgar.messaging.entity.User
import io.github.shaksternano.borgar.messaging.entity.channel.Channel
import io.github.shaksternano.borgar.messaging.entity.getContent
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import io.github.shaksternano.borgar.messaging.interaction.message.SelectMessageCommand
import io.ktor.client.request.*
import io.ktor.http.*
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
        getEmbeds().mapNotNull {
            it.getContent(getGif)
        }
    )
    addAll(
        content.getUrls().mapNotNull {
            if (isImageOrVideo(it)) {
                UrlInfo(it)
            } else {
                retrieveTenorMediaUrl(it, getGif)
            }
        }
    )
}

suspend fun CommandMessageIntersection.getEmojiAndUrlDrawables(): Map<String, Drawable> =
    if (content.isBlank()) emptyMap()
    else getEmojiDrawables() + getUrlDrawables()

private suspend fun CommandMessageIntersection.getEmojiDrawables(): Map<String, Drawable> =
    getEmojiUrls().mapValues {
        val dataSource = DataSource.fromUrl(url = it.value)
        ImageDrawable(dataSource)
    }

private suspend fun CommandMessageIntersection.getUrlDrawables(): Map<String, Drawable> {
    val embeds = getEmbeds().associateBy { it.url }
    return content.getUrls()
        .associateBy { it }
        .mapNotNull { entry ->
            var checkContentType = true
            val url = embeds[entry.key]?.getContent(false)?.url
                ?: retrieveTenorMediaUrl(entry.key, TenorMediaType.MP4_NORMAL).also {
                    checkContentType = false
                }
                ?: entry.key
            if (checkContentType && !isImageOrVideo(url)) {
                return@mapNotNull null
            }
            val dataSource = DataSource.fromUrl(url)
            runCatching {
                ImageDrawable(dataSource)
            }.map { entry.key to it }.getOrNull()
        }.associate { it }
}

private suspend fun isImageOrVideo(url: String): Boolean {
    val contentType = runCatching {
        useHttpClient { client ->
            client.head(url).contentType()
        }
    }.getOrNull() ?: return false
    return contentType.match(ContentType.Image.Any) || contentType.match(ContentType.Video.Any)
}

suspend fun CommandMessageIntersection.getEmojiUrls(): Map<String, String> {
    val emojiUrls = mutableMapOf<String, String>()

    val customEmojis = customEmojis.toSet()
    // Get custom emojis.
    customEmojis.forEach {
        emojiUrls[it.asMention] = it.imageUrl
    }

    if (manager.platform == MessagingPlatform.DISCORD) {
        // Get undetected emojis, such as those requiring Discord nitro
        getGuild()?.let { guild ->
            val existingEmojis = customEmojis.map { it.name }.toSet()
            guild.customEmojis.collect {
                if (!existingEmojis.contains(it.name)) {
                    val basicMention = it.asBasicMention
                    if (content.contains(basicMention)) {
                        emojiUrls[basicMention] = it.imageUrl
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
            val compositeUnicodeBuilder = StringBuilder()
            val compositeCodePoints = buildList {
                for (k in i..j) {
                    val codePoint = codePoints[k]
                    val hexCodePoint = codePoint.toString(16)
                    add(codePoint)
                    compositeUnicodeBuilder.append(hexCodePoint).append("-")
                }
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
    manager.emojiTypedRegex.findAll(content).forEach {
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
        CommandMessageIntersection::searchSelectedMessage,
        CommandMessageIntersection::searchSelf,
        CommandMessageIntersection::searchReferencedMessages,
        CommandMessageIntersection::searchPreviousMessages,
    )

suspend fun <T> CommandMessageIntersection.searchExceptSelf(find: suspend (CommandMessageIntersection) -> T?): T? =
    searchVisitors(
        find,
        CommandMessageIntersection::searchSelectedMessage,
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

private suspend fun <T> CommandMessageIntersection.searchSelectedMessage(
    find: suspend (CommandMessageIntersection) -> T?,
): T? =
    getChannel()?.let { channel ->
        SelectMessageCommand.getAndExpireSelectedMessage(
            authorId,
            channel.id,
            manager.platform,
        )
    }?.let {
        find(it)
    }

private suspend fun <T> CommandMessageIntersection.searchSelf(
    find: suspend (CommandMessageIntersection) -> T?,
): T? =
    find(this)

private suspend fun <T> CommandMessageIntersection.searchReferencedMessages(
    find: suspend (CommandMessageIntersection) -> T?,
): T? =
    referencedMessages
        .map {
            find(it)
        }
        .firstOrNull {
            it != null
        }

private suspend fun <T> CommandMessageIntersection.searchPreviousMessages(
    find: suspend (CommandMessageIntersection) -> T?,
): T? =
    getPreviousMessages()
        .take(MAX_PAST_MESSAGES_TO_CHECK)
        .map {
            find(it)
        }
        .firstOrNull {
            it != null
        }

suspend fun CommandEvent.getEntityId(): String =
    getEntityId(getChannel()) {
        getAuthor()
    }

suspend fun Message.getEntityId(): String {
    val channel = getChannel() ?: error("Message channel not found")
    return getEntityId(channel) {
        getAuthor()
    }
}

private suspend inline fun getEntityId(
    channel: Channel,
    authorSupplier: () -> User,
): String {
    val environment = channel.environment
    return when (environment) {
        ChannelEnvironment.GUILD -> channel.getGuild()?.id
        ChannelEnvironment.DIRECT_MESSAGE -> authorSupplier().id
        ChannelEnvironment.PRIVATE -> authorSupplier().id
        ChannelEnvironment.GROUP -> channel.getGroup()?.id
    } ?: error("Entity ID not found in environment $environment")
}

suspend inline fun checkEntityIdBelongs(
    currentEnvironmentEntityId: String,
    toCheckEntityId: String,
    targetEnvironment: ChannelEnvironment,
    authorId: String,
    manager: BotManager,
    onDoesNotBelong: () -> Unit,
) {
    if (currentEnvironmentEntityId != toCheckEntityId) {
        when (targetEnvironment) {
            ChannelEnvironment.GUILD -> {
                val guild = manager.getGuild(toCheckEntityId)
                val belongsToGuild = guild == null || guild.isMember(authorId)
                if (!belongsToGuild) {
                    onDoesNotBelong()
                }
            }

            ChannelEnvironment.DIRECT_MESSAGE -> return onDoesNotBelong()

            ChannelEnvironment.PRIVATE -> return onDoesNotBelong()

            ChannelEnvironment.GROUP -> {
                val group = manager.getGroup(toCheckEntityId)
                val belongsToGroup = group == null || group.isMember(authorId)
                if (!belongsToGroup) {
                    onDoesNotBelong()
                }
            }
        }
    }
}
