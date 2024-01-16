package io.github.shaksternano.borgar.core.collect

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

fun <T> MutableCollection<in T>.addAll(vararg elements: T): Boolean =
    addAll(elements)

suspend fun <T, R> Iterable<T>.parallelMap(transform: suspend (T) -> R): List<R> = coroutineScope {
    map { async { transform(it) } }.awaitAll()
}

fun <K, V> MutableMap<K, V>.putAllKeys(keys: Iterable<K>, value: V) = keys.forEach {
    put(it, value)
}
