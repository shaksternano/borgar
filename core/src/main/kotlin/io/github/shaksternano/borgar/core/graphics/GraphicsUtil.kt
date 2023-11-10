package io.github.shaksternano.borgar.core.graphics

import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import java.awt.Graphics2D

fun Graphics2D.fitFontWidth(maxWidth: Int, text: Drawable): Int {
    var font = this.font
    var textWidth = text.width(this)
    while (textWidth > maxWidth) {
        val sizeRatio = textWidth.toFloat() / maxWidth
        font = font.deriveFont(font.size - sizeRatio)
        this.font = font
        textWidth = text.width(this)
    }
    return textWidth
}
