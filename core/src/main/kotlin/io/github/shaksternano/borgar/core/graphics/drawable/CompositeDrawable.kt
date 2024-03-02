package io.github.shaksternano.borgar.core.graphics.drawable

import io.github.shaksternano.borgar.core.io.closeAll

abstract class CompositeDrawable : Drawable {

    val parts: MutableList<Drawable> = mutableListOf()

    override suspend fun close() =
        closeAll(parts)
}
