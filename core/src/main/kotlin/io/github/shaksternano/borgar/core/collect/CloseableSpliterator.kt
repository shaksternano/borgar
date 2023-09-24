package io.github.shaksternano.borgar.core.collect

import java.io.Closeable
import java.util.*
import java.util.function.Consumer

interface CloseableSpliterator<T> : Spliterator<T>, Closeable {

    companion object {
        private val EMPTY: CloseableSpliterator<Nothing> = wrap(Spliterators.emptySpliterator())

        fun <T> wrap(spliterator: Spliterator<T>): CloseableSpliterator<T> {
            if (spliterator is CloseableSpliterator<T>) {
                return spliterator
            }
            return CloseableSpliteratorImpl(spliterator)
        }

        fun <T> create(iterator: Iterator<T>, size: Long, characteristics: Int): CloseableSpliterator<T> {
            return CloseableSpliteratorImpl(iterator, size, characteristics)
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> empty(): CloseableSpliterator<T> = EMPTY as CloseableSpliterator<T>
    }
}

private class CloseableSpliteratorImpl<T> private constructor(
    private val spliterator: Spliterator<T>,
    private val toClose: Closeable?
) : CloseableSpliterator<T> {

    constructor(spliterator: Spliterator<T>) : this(
        spliterator,
        if (spliterator is Closeable) spliterator else null,
    )

    constructor(iterator: Iterator<T>, size: Long, characteristics: Int) : this(
        Spliterators.spliterator(iterator, size, characteristics),
        if (iterator is Closeable) iterator else null,
    )

    override fun tryAdvance(action: Consumer<in T>?): Boolean = spliterator.tryAdvance(action)

    override fun trySplit(): Spliterator<T> = spliterator.trySplit()

    override fun estimateSize(): Long = spliterator.estimateSize()

    override fun characteristics(): Int = spliterator.characteristics()

    override fun close() {
        toClose?.close()
    }
}
