package io.github.shaksternano.borgar.core.graphics.drawable

import io.github.shaksternano.borgar.core.graphics.DEFAULT_FONT_NAME
import io.github.shaksternano.borgar.core.graphics.attributedStringBounds
import io.github.shaksternano.borgar.core.graphics.createFallbackFontString
import io.github.shaksternano.borgar.core.util.kClass
import java.awt.Font
import java.awt.Graphics2D
import kotlin.math.max
import kotlin.time.Duration

class SimpleTextDrawable(
    override val text: String,
) : TextDrawable {

    override suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration) {
        val mainMetrics = graphics.fontMetrics
        if (requireFallbackFont(graphics)) {
            val mainFont = graphics.font
            val fallbackFont = Font(DEFAULT_FONT_NAME, Font.PLAIN, mainFont.size)
            val attributedString = createFallbackFontString(text, mainFont, fallbackFont)
            graphics.drawString(attributedString.iterator, x, y + mainMetrics.ascent)
        } else {
            graphics.drawString(text, x, y + mainMetrics.ascent)
        }
    }

    override suspend fun getWidth(graphics: Graphics2D): Int =
        if (requireFallbackFont(graphics)) {
            val mainFont = graphics.font
            val fallbackFont = Font(DEFAULT_FONT_NAME, Font.PLAIN, mainFont.size)
            val attributedString = createFallbackFontString(text, mainFont, fallbackFont)
            graphics.attributedStringBounds(attributedString).width.toInt()
        } else {
            graphics.fontMetrics.stringWidth(text)
        }

    override suspend fun getHeight(graphics: Graphics2D): Int {
        val mainMetrics = graphics.fontMetrics
        val mainHeight = mainMetrics.ascent + mainMetrics.descent
        return if (requireFallbackFont(graphics)) {
            val mainFont = graphics.font
            val fallbackFont = Font(DEFAULT_FONT_NAME, mainFont.style, mainFont.size)
            val fallbackMetrics = graphics.getFontMetrics(fallbackFont)
            max(
                mainHeight,
                fallbackMetrics.ascent + fallbackMetrics.descent,
            )
        } else {
            mainHeight
        }
    }

    private fun requireFallbackFont(graphics: Graphics2D): Boolean =
        graphics.font.canDisplayUpTo(text) != -1

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
