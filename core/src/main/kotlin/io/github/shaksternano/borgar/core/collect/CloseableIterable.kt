package io.github.shaksternano.borgar.core.collect

import java.io.Closeable
import java.util.function.Consumer

interface CloseableIterable<T> : Iterable<T>, Closeable {

    override fun iterator(): CloseableIterator<T>

    override fun forEach(action: Consumer<in T>) = iterator().use {
        while (it.hasNext()) {
            action.accept(it.next())
        }
    }

    override fun spliterator(): CloseableSpliterator<T>
}
