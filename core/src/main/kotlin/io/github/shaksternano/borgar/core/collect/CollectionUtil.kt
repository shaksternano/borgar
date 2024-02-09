package io.github.shaksternano.borgar.core.collect

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

fun <T> MutableCollection<in T>.addAll(vararg elements: T): Boolean =
    addAll(elements)

suspend fun <T, R> Iterable<T>.parallelMap(transform: suspend (T) -> R): List<R> =
    if (this is Collection && size <= 1) {
        map { transform(it) }
    } else {
        coroutineScope {
            map { async { transform(it) } }.awaitAll()
        }
    }

suspend fun <T> Iterable<T>.parallelForEach(action: suspend (T) -> Unit) {
    if (this is Collection && size <= 1) {
        forEach { action(it) }
    } else {
        parallelMap(action)
    }
}

fun <K, V> MutableMap<K, V>.putAllKeys(keys: Iterable<K>, value: V) = keys.forEach {
    put(it, value)
}

inline fun <T> forEachNotNull(
    supplier: () -> T?,
    action: (T) -> Unit,
) {
    var value = supplier()
    while (value != null) {
        action(value)
        value = supplier()
    }
}
