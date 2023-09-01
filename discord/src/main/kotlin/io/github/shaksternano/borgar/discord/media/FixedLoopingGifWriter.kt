package io.github.shaksternano.borgar.discord.media

import com.sksamuel.scrimage.nio.StreamingGifWriter
import java.time.Duration
import javax.imageio.metadata.IIOMetadataNode

class FixedLoopingGifWriter : StreamingGifWriter {

    private val frameDelay: Duration
    private val infiniteLoop: Boolean
    private val compressed: Boolean

    constructor() : super()

    constructor(
        frameDelay: Duration,
        infiniteLoop: Boolean,
        compressed: Boolean
    ) : super(
        frameDelay,
        infiniteLoop,
        compressed
    )

    init {
        frameDelay = readParentField("frameDelay")
        infiniteLoop = readParentField("infiniteLoop")
        compressed = readParentField("compressed")
    }

    private fun <T> readParentField(name: String): T {
        val field = javaClass.superclass.getDeclaredField(name)
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(this) as T
    }

    override fun withFrameDelay(delay: Duration): StreamingGifWriter {
        return FixedLoopingGifWriter(delay, infiniteLoop, compressed)
    }

    override fun withInfiniteLoop(infiniteLoop: Boolean): StreamingGifWriter {
        return FixedLoopingGifWriter(frameDelay, infiniteLoop, compressed)
    }

    override fun withCompression(compressed: Boolean): StreamingGifWriter {
        return FixedLoopingGifWriter(frameDelay, infiniteLoop, compressed)
    }

    override fun populateApplicationExtensions(root: IIOMetadataNode, infiniteLoop: Boolean) {
        // Only insert application extension if infinite looping is wanted
        if (infiniteLoop) {
            super.populateApplicationExtensions(root, true)
        }
    }
}
