package io.github.shaksternano.borgar.core.graphics.drawable

import io.github.shaksternano.borgar.core.graphics.Position
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import java.awt.Graphics2D
import kotlin.time.Duration

interface Drawable : SuspendCloseable {

    /**
     * Draws the drawable.
     *
     * @param graphics  The graphics object to draw on.
     * @param x         The x coordinate of the top left corner of the drawable.
     * @param y         The y coordinate of the top left corner of the drawable.
     * @param timestamp The timestamp of the frame.
     */
    suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration)

    fun getWidth(graphics: Graphics2D): Int

    fun getHeight(graphics: Graphics2D): Int

    /**
     * Creates a new drawable from this one that has been resized to the given height.
     *
     * @param height The height to resize to.
     * @return A new drawable that has been resized to the given width,
     * or null if height resizing is not supported.
     */
    fun resizeToHeight(height: Int): Drawable?

    override suspend fun close() = Unit
}

suspend fun Drawable.draw(graphics: Graphics2D, position: Position, timestamp: Duration) {
    draw(graphics, position.x, position.y, timestamp)
}
