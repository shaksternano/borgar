package io.github.shaksternano.borgar.core.graphics

import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints

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

fun Graphics2D.fitFontWidth(maxWidth: Int, text: Drawable): Int {
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

fun Graphics2D.fitFontHeight(maxHeight: Int, text: Drawable): Int {
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

data class Position(val x: Int, val y: Int)
