package io.github.shaksternano.borgar.core.media

interface VideoFrame<T, V : VideoFrame<T, V>> {

    /**
     * The content of the frame.
     *
     * @return The content of the frame.
     */
    val content: T

    /**
     * The duration of the frame in microseconds.
     *
     * @return The duration of the frame in microseconds.
     */
    val duration: Double

    /**
     * The timestamp of the frame in microseconds.
     *
     * @return The timestamp of the frame in microseconds.
     */
    val timestamp: Long

    fun transform(newContent: T): V

    fun transform(speedMultiplier: Float): V

    fun transform(newContent: T, speedMultiplier: Float): V

    fun transform(duration: Double, timestamp: Long): V
}
