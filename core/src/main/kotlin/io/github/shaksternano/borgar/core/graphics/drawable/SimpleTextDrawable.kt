package io.github.shaksternano.borgar.core.graphics.drawable

import io.github.shaksternano.borgar.core.util.kClass
import java.awt.Graphics2D
import kotlin.time.Duration

class SimpleTextDrawable(
    override val text: String,
) : TextDrawable {

    override suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration) =
        graphics.drawString(text, x, y + graphics.fontMetrics.ascent)

    override fun getWidth(graphics: Graphics2D): Int =
        graphics.fontMetrics.stringWidth(text)

    override fun getHeight(graphics: Graphics2D): Int {
        val metrics = graphics.fontMetrics
        return metrics.ascent + metrics.descent
    }

    override fun resizeToHeight(height: Int): Drawable? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false
        other as SimpleTextDrawable
        return text == other.text
    }

    override fun hashCode(): Int =
        text.hashCode()

    override fun toString(): String {
        return "TextDrawable(text='$text')"
    }
}
