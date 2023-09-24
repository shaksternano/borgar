package io.github.shaksternano.borgar.core.collect

fun <K, V> MutableMap<K, V>.putAllKeys(keys: Iterable<K>, value: V) = keys.forEach {
    put(it, value)
}
