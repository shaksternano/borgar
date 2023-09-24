package io.github.shaksternano.borgar.core.collect

interface SizedIterable<T> : Iterable<T> {
    val size: Long
}
