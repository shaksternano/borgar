package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.Attachment

interface CommandArguments {

    val defaultKey: String?

    operator fun contains(key: String): Boolean

    operator fun <T> get(key: String, argumentType: SimpleCommandArgumentType<T>): T?

    suspend fun <T> getSuspend(key: String, argumentType: CommandArgumentType<T>): T?
}

fun CommandArguments.getDefaultStringOrEmpty(): String =
    defaultKey?.let { getStringOrEmpty(it) } ?: ""

fun CommandArguments.getStringOrEmpty(key: String): String =
    this[key, CommandArgumentType.STRING] ?: ""

fun CommandArguments.getDefaultAttachment(): Attachment? = this["file", CommandArgumentType.ATTACHMENT]

fun CommandArguments.getDefaultUrl(): String? = this["url", CommandArgumentType.STRING]
