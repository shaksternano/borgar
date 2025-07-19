package io.github.shaksternano.borgar.core.collect

import com.google.common.cache.Cache
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> MutableCollection<in T>.addAll(vararg elements: T): Boolean =
    addAll(elements)

suspend fun <T, R> Iterable<T>.parallelMap(transform: suspend (T) -> R): List<R> =
    if (this is Collection && size <= 1)
        map { transform(it) }
    else coroutineScope {
        map { async { transform(it) } }.awaitAll()
    }

suspend fun <T> Iterable<T>.parallelForEach(action: suspend (T) -> Unit) {
    if (this is Collection && size <= 1)
        forEach { action(it) }
    else
        parallelMap(action)
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

operator fun <T> Flow<T>.plus(other: Flow<T>): Flow<T> = flow {
    collect {
        emit(it)
    }
    other.collect {
        emit(it)
    }
}

inline fun <K : Any, V : Any> Cache<K, V>.getOrPut(key: K, defaultValue: () -> V): V =
    getIfPresent(key)
        ?: defaultValue().also {
            put(key, it)
        }

fun <K : Any, V : Any> Cache<K, V>.getAndInvalidate(key: K): V? =
    getIfPresent(key).also {
        invalidate(key)
    }
