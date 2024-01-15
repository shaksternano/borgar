package io.github.shaksternano.borgar.core.util

import java.text.DecimalFormat
import kotlin.reflect.KClass

private val FLOAT_FORMAT = DecimalFormat("0.#")

val <T : Any> T.kClass: KClass<out T>
    get() = this::class

fun hash(vararg objects: Any?): Int {
    return if (objects.size == 1) {
        objects.single().hashCode()
    } else {
        objects.contentHashCode()
    }
}

fun <T> T.asSingletonList(): List<T> = listOf(this)

val Any.formatted: String
    get() = when (this) {
        is Float -> FLOAT_FORMAT.format(this)
        is Double -> FLOAT_FORMAT.format(this)
        else -> toString()
    }
