package io.github.shaksternano.borgar.core.graphics.drawable

import io.github.shaksternano.borgar.core.graphics.bounds
import io.github.shaksternano.borgar.core.graphics.configureTextDrawQuality
import io.github.shaksternano.borgar.core.graphics.shape
import io.github.shaksternano.borgar.core.util.hash
import io.github.shaksternano.borgar.core.util.kClass
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import kotlin.time.Duration

class OutlinedTextDrawable(
    override val text: String,
    private val fillColor: Color,
    private val outlineColor: Color,
    private val outlineWidthRatio: Double,
) : TextDrawable {

    override suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration) {
        val font = graphics.font
        val textOutlineWidth = font.size2D * outlineWidthRatio
        val actualX = x + textOutlineWidth.toInt()
        val actualY = y + graphics.fontMetrics.ascent
        val outlineStroke = BasicStroke(textOutlineWidth.toFloat())

        val originalColor = graphics.color
        val originalStroke = graphics.stroke
        val originalHints = graphics.renderingHints

        val textShape = graphics.shape(text)

        graphics.configureTextDrawQuality()
        graphics.color = outlineColor
        graphics.stroke = outlineStroke
        graphics.translate(actualX, actualY)
        graphics.draw(textShape)

        graphics.color = fillColor
        graphics.fill(textShape)

        graphics.color = originalColor
        graphics.stroke = originalStroke
        graphics.setRenderingHints(originalHints)
        graphics.translate(-actualX, -actualY)
    }

    override fun getWidth(graphics: Graphics2D): Int {
        val font = graphics.font
        val textOutlineWidth = font.size2D * outlineWidthRatio
        return (graphics.bounds(text).width + textOutlineWidth * 2).toInt()
    }

    override fun getHeight(graphics: Graphics2D): Int {
        val font = graphics.font
        val textOutlineWidth = font.size2D * outlineWidthRatio
        return (graphics.bounds(text).height + textOutlineWidth * 2).toInt()
    }

    override fun resizeToHeight(height: Int): Drawable? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false

        other as OutlinedTextDrawable

        if (text != other.text) return false
        if (fillColor != other.fillColor) return false
        if (outlineColor != other.outlineColor) return false
        if (outlineWidthRatio != other.outlineWidthRatio) return false

        return true
    }

    override fun hashCode(): Int = hash(
        text,
        fillColor,
        outlineColor,
        outlineWidthRatio,
    )

    override fun toString(): String {
        return "OutlinedTextDrawable(text='$text'" +
            ", fillColor=$fillColor" +
            ", outlineColor=$outlineColor" +
            ", outlineWidthRatio=$outlineWidthRatio)"
    }
}
