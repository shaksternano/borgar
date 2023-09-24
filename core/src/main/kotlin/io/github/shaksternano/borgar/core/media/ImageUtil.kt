package io.github.shaksternano.borgar.core.media

import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp

val BufferedImage.typeNoCustom: Int
    get() {
        val type: Int = type
        return if (type < BufferedImage.TYPE_INT_RGB || type > BufferedImage.TYPE_BYTE_INDEXED)
            BufferedImage.TYPE_INT_ARGB
        else type
    }

fun BufferedImage.convertType(type: Int): BufferedImage {
    if (this.type == type) return this
    val newType = BufferedImage(width, height, type)
    val convertOp = ColorConvertOp(null)
    return convertOp.filter(this, newType)
}
