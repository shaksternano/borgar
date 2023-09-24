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

fun BufferedImage.bound(maxDimension: Int): BufferedImage {
    return try {
        ImageUtil.bound(this, maxDimension, maxDimension)
    } catch (e: Exception) {
        val width: Int = width
        val height: Int = height
        if (width <= maxDimension && height <= maxDimension) {
            return this
        }
        val resizeRatio: Float
        resizeRatio = if (width > height) {
            maxDimension.toFloat() / width
        } else {
            maxDimension.toFloat() / height
        }
        ImageUtil.resize(this, resizeRatio, true)
    }
}
