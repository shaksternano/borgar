package io.github.shaksternano.borgar.core.graphics.drawable

import io.github.shaksternano.borgar.core.media.ImageUtil
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.Rectangle2D
import kotlin.time.Duration

class OutlinedTextDrawable(
    private val text: String,
    private val fillColor: Color,
    private val outlineColor: Color,
    private val outlineWidthRatio: Double,
) : Drawable {

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

        ImageUtil.configureTextDrawQuality(graphics)

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

    override fun getWidth(graphicsContext: Graphics2D): Int {
        val font = graphicsContext.font
        val textOutlineWidth = font.size2D * outlineWidthRatio
        return (graphicsContext.bounds(text).width + textOutlineWidth * 2).toInt()
    }

    override fun getHeight(graphicsContext: Graphics2D): Int {
        val font = graphicsContext.font
        val textOutlineWidth = font.size2D * outlineWidthRatio
        return (graphicsContext.bounds(text).height + textOutlineWidth * 2).toInt()
    }

    override fun resizeToHeight(height: Int): Drawable? = null

    private fun Graphics2D.shape(text: String): Shape =
        font.createGlyphVector(fontRenderContext, text).outline

    private fun Graphics2D.bounds(text: String): Rectangle2D =
        shape(text).bounds2D
}
