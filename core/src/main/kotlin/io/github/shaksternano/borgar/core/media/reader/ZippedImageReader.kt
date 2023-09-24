package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.collect.CloseableIterator
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.ImageFrame
import kotlin.math.max

class ZippedImageReader(
    private val firstReader: MediaReader<ImageFrame>,
    private val secondReader: MediaReader<ImageFrame>,
) : BaseImageReader() {

    private val firstControlling: Boolean =
        firstReader.isAnimated &&
            (!secondReader.isAnimated || firstReader.frameDuration <= secondReader.frameDuration)
    override val frameRate: Double = ifFirstControllingOrElse(firstReader.frameRate, secondReader.frameRate)
    override val duration: Double = ifEmptyOrElse(0.0) {
        max(firstReader.duration, secondReader.duration)
    }
    override val frameDuration: Double = ifFirstControllingOrElse(firstReader.frameDuration, secondReader.frameDuration)
    override val size: Long = ifEmptyOrElse(0) {
        (duration / frameDuration).toLong()
    }
    override val width: Int = ifFirstControllingOrElse(firstReader.width, secondReader.width)
    override val height: Int = ifFirstControllingOrElse(firstReader.height, secondReader.height)
    override val loopCount: Int =
        if (firstReader.loopCount == 0 || secondReader.loopCount == 0) 0
        else max(firstReader.loopCount, secondReader.loopCount)

    private fun <T> ifEmptyOrElse(ifEmpty: T, ifNotEmpty: () -> T): T =
        if (firstReader.isEmpty || secondReader.isEmpty) ifEmpty
        else ifNotEmpty()

    private fun <T> ifFirstControllingOrElse(ifFirstControlling: T, ifSecondControlling: T): T =
        if (firstControlling) ifFirstControlling
        else ifSecondControlling

    override fun readFrame(timestamp: Double): ImageFrame = firstReader.readFrame(timestamp)

    fun readFrame2(timestamp: Double): ImageFrame = secondReader.readFrame(timestamp)

    override fun createReversed(): ImageReader =
        ZippedImageReader(firstReader.reversed, secondReader.reversed)

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
) : CloseableIterator<ImageFrame> {

    private var firstIterator: CloseableIterator<ImageFrame>? = null
    private var secondIterator: CloseableIterator<ImageFrame>? = null
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

    override fun next(): ImageFrame {
        val firstIterator = this.firstIterator
        val secondIterator = this.secondIterator
        return if (firstIterator != null) {
            val first = firstIterator.next()
            val timestamp = first.timestamp + loops * firstReader.duration
            val second = secondReader.readFrame(timestamp)
            if (!firstIterator.hasNext() && timestamp + first.duration < secondReader.duration) {
                firstIterator.close()
                this.firstIterator = firstReader.iterator()
                loops++
            }
            this.second = second
            first
        } else if (secondIterator != null) {
            val second = secondIterator.next()
            val timestamp = second.timestamp + loops * secondReader.duration
            val first = firstReader.readFrame(timestamp)
            if (!secondIterator.hasNext() && timestamp + second.duration < firstReader.duration) {
                secondIterator.close()
                this.secondIterator = secondReader.iterator()
                loops++
            }
            this.second = second
            first
        } else {
            throw IllegalStateException("Both iterators are null")
        }
    }

    fun next2(): ImageFrame {
        if (!::second.isInitialized) {
            throw IllegalStateException("next() must be called before next2()")
        }
        return second
    }

    override fun close() = closeAll(firstReader, secondReader)
}
