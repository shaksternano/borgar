package com.shakster.borgar.core.graphics

import com.shakster.borgar.core.graphics.drawable.Drawable
import java.awt.*
import java.awt.font.LineBreakMeasurer
import java.awt.font.TextAttribute
import java.awt.geom.Rectangle2D
import java.text.AttributedString

fun Graphics2D.configureTextDrawQuality() {
    setRenderingHint(
        RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON
    )

    setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
    )

    setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON

    )

    setRenderingHint(
        RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY
    )
}

suspend fun Graphics2D.fitFontWidth(maxWidth: Int, text: Drawable): Int {
    var font = this.font
    var textWidth = text.getWidth(this)
    while (textWidth > maxWidth) {
        val sizeRatio = textWidth.toFloat() / maxWidth
        font = font.deriveFont(font.size - sizeRatio)
        this.font = font
        textWidth = text.getWidth(this)
    }
    return textWidth
}

suspend fun Graphics2D.fitFontHeight(maxHeight: Int, text: Drawable): Int {
    var font = this.font
    var textHeight = text.getHeight(this)
    while (textHeight > maxHeight) {
        val sizeRatio = textHeight.toFloat() / maxHeight
        font = font.deriveFont(font.size - sizeRatio)
        this.font = font
        textHeight = text.getHeight(this)
    }
    return textHeight
}

fun Graphics2D.fillRect(rectangle: Rectangle) {
    fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
}

fun Graphics2D.shape(text: String): Shape =
    font.createGlyphVector(fontRenderContext, text).outline

fun Graphics2D.bounds(text: String): Rectangle2D =
    shape(text).bounds2D

fun createFallbackFontString(text: String, mainFont: Font, fallbackFont: Font): AttributedString {
    val result = AttributedString(text)
    result.addAttribute(TextAttribute.FONT, mainFont, 0, text.length)
    var fallback = false
    var fallbackBegin = 0
    text.forEachIndexed { index, char ->
        val currentFallback = !mainFont.canDisplay(char)
        if (currentFallback != fallback) {
            fallback = currentFallback
            if (fallback) {
                fallbackBegin = index
            } else {
                result.addAttribute(TextAttribute.FONT, fallbackFont, fallbackBegin, index)
            }
        }
    }
    if (fallback) {
        result.addAttribute(TextAttribute.FONT, fallbackFont, fallbackBegin, text.length)
    }
    return result
}

fun Graphics2D.attributedStringBounds(attributedString: AttributedString): Rectangle2D {
    val lineBreakMeasurer = LineBreakMeasurer(attributedString.iterator, fontRenderContext)
    val textLayout = lineBreakMeasurer.nextLayout(Float.MAX_VALUE)
    return textLayout.bounds
}

data class Position(val x: Int, val y: Int)

enum class ContentPosition {
    TOP,
    CENTRE,
    BOTTOM,
}

enum class TextAlignment {
    LEFT,
    CENTRE,
    RIGHT,
}
