package io.github.shaksternano.borgar.core.media

import com.sksamuel.scrimage.ImmutableImage
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp
import kotlin.math.*

val BufferedImage.typeNoCustom: Int
    get() =
        if (type < BufferedImage.TYPE_INT_RGB || type > BufferedImage.TYPE_BYTE_INDEXED)
            BufferedImage.TYPE_INT_ARGB
        else type

fun BufferedImage.convertType(type: Int): BufferedImage =
    if (this.type == type) this
    else {
        val newType = BufferedImage(width, height, type)
        val convertOp = ColorConvertOp(null)
        convertOp.filter(this, newType)
    }

fun BufferedImage.bound(width: Int, height: Int): BufferedImage =
    ImmutableImage.wrapAwt(this).bound(width, height).awt()

fun BufferedImage.bound(maxDimension: Int): BufferedImage =
    runCatching {
        bound(maxDimension, maxDimension)
    }.getOrElse {
        if (width <= maxDimension && height <= maxDimension) {
            this
        } else {
            val maxDimensionDouble = maxDimension.toDouble()
            val resizeRatio = if (width > height) {
                maxDimensionDouble / width
            } else {
                maxDimensionDouble / height
            }
            resize(resizeRatio, true)
        }
    }

fun BufferedImage.resize(resizeMultiplier: Double): BufferedImage =
    resize(resizeMultiplier, false)

fun BufferedImage.resize(resizeMultiplier: Double, raw: Boolean): BufferedImage =
    if (resizeMultiplier == 1.0) {
        this
    } else {
        stretch(
            (width * resizeMultiplier).toInt(),
            (height * resizeMultiplier).toInt(),
            raw,
        )
    }

fun BufferedImage.resize(maxWidth: Int, maxHeight: Int): BufferedImage =
    ImmutableImage.wrapAwt(this).max(maxWidth, maxHeight).awt()

fun BufferedImage.resizeWidth(width: Int): BufferedImage =
    ImmutableImage.wrapAwt(this).scaleToWidth(width).awt()

fun BufferedImage.resizeHeight(height: Int): BufferedImage =
    ImmutableImage.wrapAwt(this).scaleToHeight(height).awt()

/**
 * Stretches an image.
 *
 * @param targetWidth  The width to stretch the image to.
 * @param targetHeight The height to stretch the image to.
 * @param raw          If false, extra processing is done to smoothen the resulting image.
 *                     If true, no extra processing is done.
 * @return The stretched image.
 */
fun BufferedImage.stretch(targetWidth: Int, targetHeight: Int, raw: Boolean): BufferedImage {
    if (width == targetWidth && height == targetHeight) return this
    val newTargetWidth = max(targetWidth, 1)
    val newTargetHeight = max(targetHeight, 1)
    return if (raw) {
        stretchRaw(newTargetWidth, newTargetHeight)
    } else {
        runCatching {
            ImmutableImage.wrapAwt(this)
                .scaleTo(newTargetWidth, newTargetHeight)
                .awt()
        }.getOrElse {
            stretchRaw(newTargetWidth, newTargetHeight)
        }
    }
}

private fun BufferedImage.stretchRaw(targetWidth: Int, targetHeight: Int): BufferedImage {
    val stretchedImage = BufferedImage(targetWidth, targetHeight, typeNoCustom)
    val graphics = stretchedImage.createGraphics()
    graphics.drawImage(this, 0, 0, targetWidth, targetHeight, null)
    graphics.dispose()
    return stretchedImage
}

fun BufferedImage.flipX(): BufferedImage =
    ImmutableImage.wrapAwt(this).flipX().awt()

fun BufferedImage.flipY(): BufferedImage =
    ImmutableImage.wrapAwt(this).flipY().awt()

fun BufferedImage.rotate(
    radians: Double,
    resultType: Int = typeNoCustom,
    backgroundColor: Color? = null,
    newWidth: Int? = null,
    newHeight: Int? = null,
): BufferedImage {
    val sin = abs(sin(radians))
    val cos = abs(cos(radians))

    val resultWidth = newWidth ?: floor(width * cos + height * sin).toInt()
    val resultHeight = newHeight ?: floor(height * cos + width * sin).toInt()

    val rotated = BufferedImage(resultWidth, resultHeight, resultType)
    val graphics = rotated.createGraphics()

    if (backgroundColor != null) {
        graphics.color = backgroundColor
        graphics.fillRect(0, 0, resultWidth, resultHeight)
    }

    graphics.translate((resultWidth - width) / 2, (resultHeight - height) / 2)
    graphics.rotate(radians, width / 2.0, height / 2.0)
    graphics.drawRenderedImage(this, null)
    graphics.dispose()

    return rotated
}

fun BufferedImage.copy(): BufferedImage {
    val copy = copySize()
    val graphics = copy.createGraphics()
    graphics.drawImage(this, 0, 0, null)
    graphics.dispose()
    return copy
}

private fun BufferedImage.copySize(): BufferedImage =
    BufferedImage(width, height, typeNoCustom)

/**
 * Gets the distance between two colors.
 *
 * @return A value between 0 and 765 representing the distance between the two colors.
 */
infix fun Color.distanceTo(other: Color): Double =
    if (this == other) {
        0.0
    } else {
        val red1 = red
        val red2 = other.red
        val redMean = (red1 + red2) / 2
        val redDifference = red1 - red2
        val greenDifference = green - other.green
        val blueDifference = blue - other.blue
        sqrt(
            (
                (((512 + redMean) * redDifference * redDifference) shr 8)
                    + (4 * greenDifference * greenDifference)
                    + (((767 - redMean) * blueDifference * blueDifference) shr 8)
                ).toDouble()
        )
    }

val Color.isGreyScale: Boolean
    get() {
        val tolerance = 40
        return abs((red - green).toDouble()) <= tolerance
            && abs((red - blue).toDouble()) <= tolerance
            && abs((green - blue).toDouble()) <= tolerance
    }

inline fun BufferedImage.forEachPixel(action: (x: Int, y: Int) -> Unit) {
    for (y in 0..<height) {
        for (x in 0..<width) {
            action(x, y)
        }
    }
}
