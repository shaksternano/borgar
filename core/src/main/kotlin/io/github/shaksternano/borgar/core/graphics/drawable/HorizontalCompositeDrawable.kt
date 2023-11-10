package io.github.shaksternano.borgar.core.graphics.drawable

import java.awt.Graphics2D
import kotlin.math.max
import kotlin.time.Duration

class HorizontalCompositeDrawable : CompositeDrawable() {

    override suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration) {
        var drawableX = x
        parts.forEach {
            it.draw(graphics, drawableX, y, timestamp)
            drawableX += it.getWidth(graphics)
        }
    }

    override fun getWidth(graphicsContext: Graphics2D): Int =
        parts.fold(0) { width, part ->
            width + part.getWidth(graphicsContext)
        }

    override fun getHeight(graphicsContext: Graphics2D): Int =
        parts.fold(0) { height, part ->
            max(height, part.getHeight(graphicsContext))
        }

    override fun resizeToHeight(height: Int): Drawable {
        val resized = HorizontalCompositeDrawable()
        var resizedAny = false
        parts.forEach {
            val resizedPart = it.resizeToHeight(height)?.also {
                resizedAny = true
            } ?: it
            resized.parts.add(resizedPart)
        }
        return if (resizedAny) {
            resized
        } else {
            this
        }
    }
}
