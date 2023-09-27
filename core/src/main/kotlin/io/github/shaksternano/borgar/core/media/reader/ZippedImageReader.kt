package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.AsyncCloseableIterator
import io.github.shaksternano.borgar.core.collect.CloseableIterator
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.ImageFrame
import kotlinx.coroutines.*
import kotlin.math.max
import kotlin.time.Duration

class ZippedImageReader(
    private val firstReader: MediaReader<ImageFrame>,
    private val secondReader: MediaReader<ImageFrame>,
) : BaseImageReader() {

    private val firstControlling: Boolean =
        firstReader.isAnimated &&
            (!secondReader.isAnimated || firstReader.frameDuration <= secondReader.frameDuration)
    override val frameRate: Double = ifFirstControllingOrElse(firstReader.frameRate, secondReader.frameRate)
    override val duration: Duration = ifEmptyOrElse(Duration.ZERO) {
        if (firstReader.duration > secondReader.duration) firstReader.duration
        else secondReader.duration
    }
    override val frameDuration: Duration =
        ifFirstControllingOrElse(firstReader.frameDuration, secondReader.frameDuration)
    override val size: Int = ifEmptyOrElse(0) {
        (duration / frameDuration).toInt()
    }
    override val width: Int = ifFirstControllingOrElse(firstReader.width, secondReader.width)
    override val height: Int = ifFirstControllingOrElse(firstReader.height, secondReader.height)
    override val loopCount: Int =
        if (firstReader.loopCount == 0 || secondReader.loopCount == 0) 0
        else max(firstReader.loopCount, secondReader.loopCount)
    override var reversed: MediaReader<ImageFrame> = super.reversed

    private fun <T> ifEmptyOrElse(ifEmpty: T, ifNotEmpty: () -> T): T =
        if (firstReader.isEmpty || secondReader.isEmpty) ifEmpty
        else ifNotEmpty()

    private fun <T> ifFirstControllingOrElse(ifFirstControlling: T, ifSecondControlling: T): T =
        if (firstControlling) ifFirstControlling
        else ifSecondControlling

    override suspend fun readFrame(timestamp: Duration): ImageFrame = firstReader.readFrame(timestamp)

    suspend fun readFrame2(timestamp: Duration): ImageFrame = secondReader.readFrame(timestamp)

    override fun createReversed(): ImageReader {
        val reader = ZippedImageReader(firstReader.reversed, secondReader.reversed)
        reader.reversed = this
        return reader
    }

    override fun iterator(): ZippedImageReaderIterator =
        ZippedImageReaderIterator(firstReader, secondReader, firstControlling)

    override fun close() {
        closeAll(firstReader, secondReader)
    }
}

class ZippedImageReaderIterator(
    private val firstReader: MediaReader<ImageFrame>,
    private val secondReader: MediaReader<ImageFrame>,
    firstControlling: Boolean,
) : AsyncCloseableIterator<ImageFrame>(CoroutineScope(Dispatchers.IO)) {

    private var firstIterator: CloseableIterator<Deferred<ImageFrame>>? = null
    private var secondIterator: CloseableIterator<Deferred<ImageFrame>>? = null
    private var loops: Int = 0
    private lateinit var second: ImageFrame

    init {
        if (!firstReader.isEmpty && !secondReader.isEmpty) {
            if (firstControlling) {
                firstIterator = firstReader.iterator()
            } else {
                secondIterator = secondReader.iterator()
            }
        }
    }

    override fun hasNext(): Boolean {
        return firstIterator?.hasNext() ?: (secondIterator?.hasNext() ?: false)
    }

    override fun next(): Deferred<ImageFrame> {
        val firstIterator = this.firstIterator
        val secondIterator = this.secondIterator
        return async {
            if (firstIterator != null) {
                val first = firstIterator.next().await()
                val timestamp = firstReader.duration * loops + first.timestamp
                val second = secondReader.readFrame(timestamp)
                if (!firstIterator.hasNext() && timestamp + first.duration < secondReader.duration) {
                    firstIterator.close()
                    this@ZippedImageReaderIterator.firstIterator = firstReader.iterator()
                    loops++
                }
                this@ZippedImageReaderIterator.second = second
                first
            } else if (secondIterator != null) {
                val second = secondIterator.next().await()
                val timestamp = secondReader.duration * loops + second.timestamp
                val first = firstReader.readFrame(timestamp)
                if (!secondIterator.hasNext() && timestamp + second.duration < firstReader.duration) {
                    secondIterator.close()
                    this@ZippedImageReaderIterator.secondIterator = secondReader.iterator()
                    loops++
                }
                this@ZippedImageReaderIterator.second = second
                first
            } else {
                throw IllegalStateException("Both iterators are null")
            }
        }
    }

    fun next2(): Deferred<ImageFrame> {
        if (!::second.isInitialized) {
            throw IllegalStateException("next() must be called before next2()")
        }
        return CompletableDeferred(second)
    }

    override fun close() = closeAll(firstReader, secondReader)
}
