package io.github.shaksternano.borgar.core.io

import io.ktor.utils.io.*
import kotlinx.io.Buffer
import kotlinx.io.Source

abstract class DelegatedByteReadChannel : ByteReadChannel {

    private lateinit var delegate: ByteReadChannel
    private val isInitialized: Boolean
        get() = this::delegate.isInitialized

    override val closedCause: Throwable?
        get() =
            if (isInitialized) delegate.closedCause
            else null
    override val isClosedForRead: Boolean
        get() =
            if (isInitialized) delegate.isClosedForRead
            else false

    @InternalAPI
    override val readBuffer: Source
        get() =
            if (isInitialized) delegate.readBuffer
            else Buffer()

    protected abstract suspend fun createChannel(): ByteReadChannel

    private suspend fun init() {
        if (!isInitialized) delegate = createChannel()
    }

    override suspend fun awaitContent(min: Int): Boolean {
        init()
        return delegate.awaitContent(min)
    }

    override fun cancel(cause: Throwable?) {
        if (isInitialized) {
            delegate.cancel(cause)
        }
    }
}
