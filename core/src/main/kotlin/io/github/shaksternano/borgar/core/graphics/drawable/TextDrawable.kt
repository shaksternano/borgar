package io.github.shaksternano.borgar.core.graphics.drawable

import java.awt.Graphics2D
import kotlin.time.Duration

class TextDrawable(
    private val text: String,
) : Drawable {

    override suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration) =
        graphics.drawString(text, x, y + graphics.fontMetrics.ascent)

    override fun width(graphicsContext: Graphics2D): Int =
        graphicsContext.fontMetrics.stringWidth(text)

    override fun height(graphicsContext: Graphics2D): Int {
        val metrics = graphicsContext.fontMetrics
        return metrics.ascent + metrics.descent
    }

    override fun resizeToHeight(height: Int): Drawable? = null
}
