package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.messaging.entity.Attachment

interface CommandArguments {

    val defaultKey: String?
    val typedForm: String

    operator fun contains(key: String): Boolean

    operator fun <T> get(key: String, argumentType: SimpleCommandArgumentType<T>): T?

    suspend fun <T> getSuspend(key: String, argumentType: CommandArgumentType<T>): T?
}

fun CommandArguments.getDefaultStringOrEmpty(): String =
    defaultKey?.let { getStringOrEmpty(it) } ?: ""

fun CommandArguments.getStringOrEmpty(key: String): String =
    this[key, CommandArgumentType.String] ?: ""

fun CommandArguments.getDefaultAttachment(): Attachment? = this["file", CommandArgumentType.Attachment]

fun CommandArguments.getDefaultUrl(): String? = this["url", CommandArgumentType.String]
