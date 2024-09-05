package io.github.shaksternano.borgar.messaging.util

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.github.shaksternano.borgar.core.collect.getAndInvalidate
import io.github.shaksternano.borgar.messaging.MessagingPlatform
import io.github.shaksternano.borgar.messaging.entity.Message
import java.time.Duration

private data class SelectedMessageKey(
    val userId: String,
    val channelId: String,
    val platform: MessagingPlatform,
)

private val selectedMessages: Cache<SelectedMessageKey, Message> = CacheBuilder.newBuilder()
    .expireAfterWrite(Duration.ofHours(1))
    .build()
private val lowPrioritySelectedMessages: Cache<SelectedMessageKey, Message> = CacheBuilder.newBuilder()
    .expireAfterWrite(Duration.ofHours(1))
    .build()

fun setSelectedMessage(userId: String, channelId: String, platform: MessagingPlatform, message: Message) {
    selectedMessages.put(
        SelectedMessageKey(
            userId = userId,
            channelId = channelId,
            platform = platform,
        ),
        message,
    )
}

fun getAndExpireSelectedMessage(
    userId: String,
    channelId: String,
    platform: MessagingPlatform,
): Message? {
    val key = SelectedMessageKey(userId, channelId, platform)
    return selectedMessages.getAndInvalidate(key)
}

fun setLowPrioritySelectedMessage(userId: String, channelId: String, platform: MessagingPlatform, message: Message) {
    lowPrioritySelectedMessages.put(
        SelectedMessageKey(
            userId = userId,
            channelId = channelId,
            platform = platform,
        ),
        message,
    )
}

fun getAndExpireLowPrioritySelectedMessage(
    userId: String,
    channelId: String,
    platform: MessagingPlatform,
): Message? {
    val key = SelectedMessageKey(userId, channelId, platform)
    return lowPrioritySelectedMessages.getAndInvalidate(key)
}
