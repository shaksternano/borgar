package io.github.shaksternano.borgar.core.graphics.drawable

import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.util.kClass
import java.awt.Graphics2D
import kotlin.math.max
import kotlin.time.Duration

class HorizontalCompositeDrawable(
    private val parts: Iterable<Drawable>,
) : Drawable {

    override suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration) {
        var drawableX = x
        parts.forEach {
            it.draw(graphics, drawableX, y, timestamp)
            drawableX += it.getWidth(graphics)
        }
    }

    override fun getWidth(graphics: Graphics2D): Int =
        parts.fold(0) { width, part ->
            width + part.getWidth(graphics)
        }

    override fun getHeight(graphics: Graphics2D): Int =
        parts.fold(0) { height, part ->
            max(height, part.getHeight(graphics))
        }

    override fun resizeToHeight(height: Int): Drawable {
        var resizedAny = false
        val resizedParts = parts.map {
            it.resizeToHeight(height)?.also {
                resizedAny = true
            } ?: it
        }
        return if (resizedAny) {
            HorizontalCompositeDrawable(resizedParts)
        } else {
            this
        }
    }

    override suspend fun close() =
        closeAll(parts)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false

        other as HorizontalCompositeDrawable

        return parts == other.parts
    }

    override fun hashCode(): Int =
        parts.hashCode()

    override fun toString(): String {
        return "HorizontalCompositeDrawable(parts=$parts)"
    }
}
