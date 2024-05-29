package io.github.shaksternano.borgar.core.graphics.drawable

import io.github.shaksternano.borgar.core.graphics.DEFAULT_FONT_NAME
import io.github.shaksternano.borgar.core.graphics.bounds
import io.github.shaksternano.borgar.core.graphics.configureTextDrawQuality
import io.github.shaksternano.borgar.core.graphics.shape
import io.github.shaksternano.borgar.core.util.hash
import io.github.shaksternano.borgar.core.util.kClass
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import kotlin.time.Duration

class OutlinedTextDrawable(
    override val text: String,
    private val fillColor: Color,
    private val outlineColor: Color,
    private val outlineWidthRatio: Double,
) : TextDrawable {

    override suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration) {
        val originalFont = graphics.font
        val originalColor = graphics.color
        val originalStroke = graphics.stroke
        val originalHints = graphics.renderingHints

        var x1 = x.toDouble()
        val splitText = splitTextFallbackFont(graphics)
        splitText.forEach { (substring, font) ->
            graphics.font = font
            val textOutlineWidth = font.size2D * outlineWidthRatio
            val actualX = x1 + textOutlineWidth
            val actualY = (y + graphics.fontMetrics.ascent).toDouble()
            val outlineStroke = BasicStroke(textOutlineWidth.toFloat())

            val textShape = graphics.shape(substring)

            graphics.configureTextDrawQuality()
            graphics.color = outlineColor
            graphics.stroke = outlineStroke
            graphics.translate(actualX, actualY)
            graphics.draw(textShape)

            graphics.color = fillColor
            graphics.fill(textShape)

            graphics.translate(-actualX, -actualY)
            x1 += graphics.bounds(substring).width + textOutlineWidth
        }

        graphics.font = originalFont
        graphics.color = originalColor
        graphics.stroke = originalStroke
        graphics.setRenderingHints(originalHints)
    }

    override fun getWidth(graphics: Graphics2D): Int {
        val originalFont = graphics.font
        val width = splitTextFallbackFont(graphics).sumOf { (substring, font) ->
            graphics.font = font
            val textOutlineWidth = font.size2D * outlineWidthRatio
            graphics.bounds(substring).width + textOutlineWidth * 2
        }
        graphics.font = originalFont
        return width.toInt()
    }

    override fun getHeight(graphics: Graphics2D): Int {
        val originalFont = graphics.font
        val height = splitTextFallbackFont(graphics).maxOf { (substring, font) ->
            graphics.font = font
            val textOutlineWidth = font.size2D * outlineWidthRatio
            graphics.bounds(substring).height + textOutlineWidth * 2
        }
        graphics.font = originalFont
        return height.toInt()
    }

    private fun splitTextFallbackFont(graphics: Graphics2D): List<Pair<String, Font>> {
        val mainFont = graphics.font
        val fallbackFont = Font(DEFAULT_FONT_NAME, Font.PLAIN, mainFont.size)
        val splitText = mutableListOf<Pair<String, Font>>()
        var substring = ""
        var currentFont = mainFont
        text.forEach {
            if (mainFont.canDisplay(it)) {
                if (currentFont == mainFont || substring.isEmpty()) {
                    substring += it
                } else {
                    splitText.add(substring to currentFont)
                    substring = it.toString()
                }
                currentFont = mainFont
            } else {
                if (currentFont == fallbackFont || substring.isEmpty()) {
                    substring += it
                } else {
                    splitText.add(substring to currentFont)
                    substring = it.toString()
                }
                currentFont = fallbackFont
            }
        }
        if (substring.isNotEmpty()) {
            splitText.add(substring to currentFont)
        }
        return splitText
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
