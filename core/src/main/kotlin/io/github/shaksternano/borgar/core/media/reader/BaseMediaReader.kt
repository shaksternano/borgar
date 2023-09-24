package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.elementsEqual
import io.github.shaksternano.borgar.core.collect.hashElements
import io.github.shaksternano.borgar.core.media.VideoFrame
import io.github.shaksternano.borgar.core.util.hash

abstract class BaseMediaReader<T : VideoFrame<*>> : MediaReader<T> {

    override val reversed: MediaReader<T> by lazy(::createReversed)

    protected abstract fun createReversed(): MediaReader<T>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is MediaReader<*>) {
            return elementsEqual(other)
                && format == other.format
        }
        return false
    }

    override fun hashCode(): Int = hash(
        hashElements(),
        format,
    )
}
