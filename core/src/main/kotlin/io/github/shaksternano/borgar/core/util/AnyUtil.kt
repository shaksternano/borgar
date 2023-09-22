package io.github.shaksternano.borgar.core.util

import kotlin.reflect.KClass

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
