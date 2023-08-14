package io.github.shaksternano.borgar.io

import java.io.InputStream

open class IndexedInputStream(
    private val inputStream: InputStream
) : InputStream() {

    var nextIndex = 0L
        private set
    private var markIndex = 0L

    override fun read(): Int {
        val byte = inputStream.read()
        if (byte != -1) {
            nextIndex++
        }
        return byte
    }

    override fun skip(n: Long): Long {
        val skipped = inputStream.skip(n)
        nextIndex += skipped
        return skipped
    }

    override fun available(): Int {
        return inputStream.available()
    }

    override fun close() {
        inputStream.close()
    }

    override fun mark(readlimit: Int) {
        inputStream.mark(readlimit)
        markIndex = nextIndex
    }

    override fun reset() {
        inputStream.reset()
        nextIndex = markIndex
    }

    override fun markSupported(): Boolean {
        return inputStream.markSupported()
    }
}

fun InputStream.indexed(): IndexedInputStream = IndexedInputStream(this)
