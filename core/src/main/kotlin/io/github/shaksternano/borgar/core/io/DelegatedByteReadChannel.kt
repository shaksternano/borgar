package io.github.shaksternano.borgar.core.io

import io.ktor.utils.io.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer

abstract class DelegatedByteReadChannel : ByteReadChannel {

    private lateinit var delegate: ByteReadChannel
    private val isInitialized: Boolean
        get() = this::delegate.isInitialized

    override val availableForRead: Int
        get() =
            if (isInitialized) delegate.availableForRead
            else 0
    override val closedCause: Throwable?
        get() =
            if (isInitialized) delegate.closedCause
            else null
    override val isClosedForRead: Boolean
        get() =
            if (isInitialized) delegate.isClosedForRead
            else false
    override val isClosedForWrite: Boolean
        get() =
            if (isInitialized) delegate.isClosedForWrite
            else false
    override val totalBytesRead: Long
        get() =
            if (isInitialized) delegate.totalBytesRead
            else 0

    protected abstract suspend fun createChannel(): ByteReadChannel

    private suspend fun init() {
        if (!isInitialized) delegate = createChannel()
    }

    override suspend fun awaitContent() {
        init()
        delegate.awaitContent()
    }

    override fun cancel(cause: Throwable?): Boolean =
        if (isInitialized) delegate.cancel(cause)
        else true

    override suspend fun discard(max: Long): Long {
        init()
        return delegate.discard(max)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use read { } instead.")
    override fun <R> lookAhead(visitor: LookAheadSession.() -> R): R {
        runBlocking {
            init()
        }
        return delegate.lookAhead(visitor)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use read { } instead.")
    override suspend fun <R> lookAheadSuspend(visitor: suspend LookAheadSuspendSession.() -> R): R {
        init()
        return delegate.lookAheadSuspend(visitor)
    }

    override suspend fun peekTo(
        destination: Memory,
        destinationOffset: Long,
        offset: Long,
        min: Long,
        max: Long
    ): Long {
        init()
        return delegate.peekTo(destination, destinationOffset, offset, min, max)
    }

    override suspend fun read(min: Int, consumer: (ByteBuffer) -> Unit) {
        init()
        delegate.read(min, consumer)
    }

    @Suppress("DEPRECATION")
    override suspend fun readAvailable(dst: ChunkBuffer): Int {
        init()
        return delegate.readAvailable(dst)
    }

    override suspend fun readAvailable(dst: ByteBuffer): Int {
        init()
        return delegate.readAvailable(dst)
    }

    override suspend fun readAvailable(dst: ByteArray, offset: Int, length: Int): Int {
        init()
        return delegate.readAvailable(dst, offset, length)
    }

    override fun readAvailable(min: Int, block: (ByteBuffer) -> Unit): Int =
        if (isInitialized) delegate.readAvailable(min, block)
        else -1

    override suspend fun readBoolean(): Boolean {
        init()
        return delegate.readBoolean()
    }

    override suspend fun readByte(): Byte {
        init()
        return delegate.readByte()
    }

    override suspend fun readDouble(): Double {
        init()
        return delegate.readDouble()
    }

    override suspend fun readFloat(): Float {
        init()
        return delegate.readFloat()
    }

    @Suppress("DEPRECATION")
    override suspend fun readFully(dst: ChunkBuffer, n: Int) {
        init()
        delegate.readFully(dst, n)
    }

    override suspend fun readFully(dst: ByteBuffer): Int {
        init()
        return delegate.readFully(dst)
    }

    override suspend fun readFully(dst: ByteArray, offset: Int, length: Int) {
        init()
        delegate.readFully(dst, offset, length)
    }

    override suspend fun readInt(): Int {
        init()
        return delegate.readInt()
    }

    override suspend fun readLong(): Long {
        init()
        return delegate.readLong()
    }

    override suspend fun readPacket(size: Int): ByteReadPacket {
        init()
        return delegate.readPacket(size)
    }

    override suspend fun readRemaining(limit: Long): ByteReadPacket {
        init()
        return delegate.readRemaining(limit)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use read { } instead.")
    override fun readSession(consumer: ReadSession.() -> Unit) {
        runBlocking {
            init()
        }
        delegate.readSession(consumer)
    }

    override suspend fun readShort(): Short {
        init()
        return delegate.readShort()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use read { } instead.")
    override suspend fun readSuspendableSession(consumer: suspend SuspendableReadSession.() -> Unit) {
        init()
        delegate.readSuspendableSession(consumer)
    }

    override suspend fun readUTF8Line(limit: Int): String? {
        init()
        return delegate.readUTF8Line(limit)
    }

    override suspend fun <A : Appendable> readUTF8LineTo(out: A, limit: Int): Boolean {
        init()
        return delegate.readUTF8LineTo(out, limit)
    }
}
