package io.github.shaksternano.borgar.core.collect

import com.google.common.collect.Iterators
import java.io.Closeable
import java.util.*

interface CloseableIterator<T> : Iterator<T>, Closeable {
    companion object {
        private val EMPTY: CloseableIterator<Nothing> = wrap(Collections.emptyIterator())

        fun <T> wrap(iterator: Iterator<T>): CloseableIterator<T> {
            if (iterator is CloseableIterator<T>) {
                return iterator
            }
            return object : CloseableIterator<T> {

                override fun hasNext(): Boolean = iterator.hasNext()

                override fun next(): T = iterator.next()

                override fun close() {
                    if (iterator is Closeable) {
                        iterator.close()
                    }
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> empty(): CloseableIterator<T> = EMPTY as CloseableIterator<T>

        fun <T> singleton(value: T): CloseableIterator<T> = wrap(Iterators.singletonIterator(value))
    }
}
