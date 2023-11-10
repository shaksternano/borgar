package io.github.shaksternano.borgar.core.graphics.drawable

import java.awt.Graphics2D
import kotlin.math.max
import kotlin.time.Duration

class HorizontalCompositeDrawable : CompositeDrawable() {

    override suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration) {
        var drawableX = x
        parts.forEach {
            it.draw(graphics, drawableX, y, timestamp)
            drawableX += it.width(graphics)
        }
    }

    override fun width(graphicsContext: Graphics2D): Int =
        parts.fold(0) { width, part ->
            width + part.width(graphicsContext)
        }

    override fun height(graphicsContext: Graphics2D): Int =
        parts.fold(0) { height, part ->
            max(height, part.height(graphicsContext))
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
