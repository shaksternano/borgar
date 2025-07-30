package com.shakster.borgar.core.media

import com.shakster.borgar.core.graphics.*
import com.shakster.borgar.core.graphics.drawable.Drawable
import com.shakster.borgar.core.graphics.drawable.ParagraphDrawable
import com.shakster.borgar.core.media.template.Template
import com.sksamuel.scrimage.ImmutableImage
import java.awt.*
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp
import kotlin.math.*
import kotlin.time.Duration

val BufferedImage.typeNoCustom: Int
    get() =
        if (type < BufferedImage.TYPE_INT_RGB || type > BufferedImage.TYPE_BYTE_INDEXED)
            BufferedImage.TYPE_INT_ARGB
        else type

inline fun BufferedImage.forEachPixel(action: (x: Int, y: Int, rgb: Int) -> Unit) {
    for (y in 0 until height) {
        for (x in 0 until width) {
            action(x, y, getRGB(x, y))
        }
    }
}

inline fun BufferedImage.mapPixels(transform: (rgb: Int) -> Int): BufferedImage {
    val newImage = copySize()
    forEachPixel { x, y, rgb ->
        newImage.setRGB(x, y, transform(rgb))
    }
    return newImage
}

fun BufferedImage.convertType(type: Int): BufferedImage =
    if (this.type == type) this
    else {
        val newType = BufferedImage(width, height, type)
        val convertOp = ColorConvertOp(null)
        convertOp.filter(this, newType)
    }

fun BufferedImage.addTransparency(): BufferedImage =
    convertType(BufferedImage.TYPE_INT_ARGB)

fun BufferedImage.bound(width: Int, height: Int): BufferedImage =
    if (this.width <= width && this.height <= height) this
    else ImmutableImage.wrapAwt(this).bound(width, height).awt()

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
    if (width == maxWidth && height == maxHeight) this
    else ImmutableImage.wrapAwt(this).max(maxWidth, maxHeight).awt()

fun BufferedImage.resizeWidth(width: Int): BufferedImage =
    if (this.width == width) this
    else ImmutableImage.wrapAwt(this).scaleToWidth(width).awt()

fun BufferedImage.resizeHeight(height: Int): BufferedImage =
    if (this.height == height) this
    else ImmutableImage.wrapAwt(this).scaleToHeight(height).awt()

/**
 * Stretches an image.
 *
 * @param targetWidth  The width to stretch the image to.
 * @param targetHeight The height to stretch the image to.
 * @param raw          If false, extra processing is done to smoothen the resulting image.
 *                     If true, no extra processing is done.
 * @return The stretched image.
 */
fun BufferedImage.stretch(
    targetWidth: Int,
    targetHeight: Int,
    raw: Boolean = false,
): BufferedImage {
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

fun BufferedImage.copySize(): BufferedImage =
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

fun BufferedImage.fill(color: Color): BufferedImage {
    val filledImage = BufferedImage(width, height, typeNoCustom)
    val graphics = filledImage.createGraphics()
    graphics.color = color
    graphics.fillRect(0, 0, filledImage.width, filledImage.height)
    graphics.drawImage(this, 0, 0, null)
    graphics.dispose()
    return filledImage
}

/**
 * Calculates information used for overlaying an image on top of another image.
 *
 * @param image1    The first image.
 * @param image2    The second image.
 * @param x2        The x coordinate of the top left corner of the second image in relation to the top left corner of the first image.
 * @param y2        The y coordinate of the top left corner of the second image in relation to the top left corner of the first image.
 * @param expand    Whether to expand the resulting image to fit the second image in the case that it oversteps the boundaries of the first image.
 * @param imageType The type of the resulting image.
 * @return The overlay information.
 */
fun getOverlayData(
    image1: BufferedImage,
    image2: BufferedImage,
    x2: Int,
    y2: Int,
    expand: Boolean,
    imageType: Int? = null,
): OverlayData {
    val image1Width = image1.width
    val image1Height = image1.height

    val image2Width = image2.width
    val image2Height = image2.height

    val type = imageType ?: image1.typeNoCustom

    val overlaidWidth: Int
    val overlaidHeight: Int

    val image1X: Int
    val image1Y: Int

    val image2X: Int
    val image2Y: Int

    if (expand) {
        if (x2 < 0) {
            overlaidWidth = max(image1Width - x2, image2Width)
            image1X = -x2
        } else {
            overlaidWidth = max(image1Width, image2Width + x2)
            image1X = 0
        }

        if (y2 < 0) {
            overlaidHeight = max(image1Height - y2, image2Height)
            image1Y = -y2
        } else {
            overlaidHeight = max(image1Height, image2Height + y2)
            image1Y = 0
        }

        image2X = max(x2, 0)
        image2Y = max(y2, 0)
    } else {
        overlaidWidth = image1Width
        overlaidHeight = image1Height

        image1X = 0
        image1Y = 0

        image2X = x2
        image2Y = y2
    }

    return OverlayData(
        overlaidWidth,
        overlaidHeight,
        image1X,
        image1Y,
        image2X,
        image2Y,
        type,
    )
}

/**
 * Overlays an image on top of another image.
 *
 * @param image1             The first image.
 * @param image2             The second image.
 * @param overlayData        Additional information used for overlaying the images.
 * @param image1IsBackground Whether the first image is the background or not. If the first image is not the background, then the second image is.
 * @param image2Clip         The clipping area of the second image.
 * @param fill               The background color.
 * @return The overlaid image.
 */
fun overlay(
    image1: BufferedImage,
    image2: BufferedImage,
    overlayData: OverlayData,
    image1IsBackground: Boolean,
    image2Clip: Shape? = null,
    fill: Color? = null,
): BufferedImage {
    val overlaidImage = BufferedImage(
        overlayData.overlaidWidth,
        overlayData.overlaidHeight,
        overlayData.overlaidImageType,
    )
    val graphics = overlaidImage.createGraphics()

    if (fill != null) {
        graphics.color = fill
        if (image2Clip == null) {
            graphics.fillRect(
                0,
                0,
                overlayData.overlaidWidth,
                overlayData.overlaidHeight
            )
        } else {
            graphics.fill(image2Clip)
        }
    }

    if (image1IsBackground) {
        graphics.drawImage(
            image1,
            overlayData.image1X,
            overlayData.image1Y,
            null
        )
        if (image2Clip != null) {
            graphics.clip = image2Clip
        }
        graphics.drawImage(
            image2,
            overlayData.image2X,
            overlayData.image2Y,
            null
        )
    } else {
        if (image2Clip != null) {
            graphics.clip = image2Clip
        }
        graphics.drawImage(
            image2,
            overlayData.image2X,
            overlayData.image2Y,
            null
        )
        graphics.clip = null
        graphics.drawImage(
            image1,
            overlayData.image1X,
            overlayData.image1Y,
            null
        )
    }

    graphics.dispose()
    return overlaidImage
}

fun BufferedImage.cutout(
    imageToCutout: BufferedImage,
    x: Int,
    y: Int,
    cutoutColor: Int,
): BufferedImage {
    val imageToCut = addTransparency()

    val toCutWidth = imageToCut.width
    val toCutHeight = imageToCut.height

    val toCutoutWidth = imageToCutout.width
    val toCutoutHeight = imageToCutout.height

    val toCutPixels = imageToCut.getRGB(0, 0, toCutWidth, toCutHeight, null, 0, toCutWidth)
    val toCutoutPixels = imageToCutout.getRGB(0, 0, toCutoutWidth, toCutoutHeight, null, 0, toCutoutWidth)

    toCutoutPixels.forEachIndexed { i, toCutoutRgb ->
        if (!isTransparent(toCutoutRgb)) {
            val toCutIndex = get1dIndex(
                min(toCutWidth, x + getX(i, toCutWidth)),
                min(toCutHeight, y + getY(i, toCutWidth)),
                toCutWidth,
            )
            if (toCutIndex < toCutPixels.size) {
                toCutPixels[toCutIndex] = cutoutColor
            }
        }
    }

    imageToCut.setRGB(0, 0, toCutWidth, toCutHeight, toCutPixels, 0, toCutWidth)
    return imageToCut
}

private fun isTransparent(rgb: Int): Boolean =
    (rgb shr 24) == 0

private fun get1dIndex(x: Int, y: Int, width: Int): Int =
    y * width + x

private fun getX(index: Int, width: Int): Int =
    index % width

private fun getY(index: Int, width: Int): Int =
    index / width

data class TextDrawData(
    val text: Drawable,
    val textX: Int,
    val textY: Int,
    val textCentreX: Int,
    val textCentreY: Int,
    val font: Font,
)

suspend fun getTextDrawData(
    image: BufferedImage,
    text: String,
    nonTextParts: Map<String, Drawable>,
    template: Template,
): TextDrawData {
    val paragraph = ParagraphDrawable(
        text,
        nonTextParts,
        template.textContentAlignment,
        template.textContentWidth,
    )

    val graphics = image.createGraphics()

    val font = template.font
    graphics.font = font
    graphics.configureTextDrawQuality()

    graphics.fitFontWidth(template.textContentWidth, paragraph)
    graphics.fitFontHeight(template.textContentHeight, paragraph)
    val paragraphHeight = graphics.fitFontHeight(template.textContentHeight, paragraph)
    val fontSize = graphics.font.size2D

    graphics.dispose()

    val resizedFont = font.deriveFont(fontSize)

    val containerCentreY = template.textContentY + (template.textContentHeight / 2)

    val textX = template.textContentX
    val textY = when (template.textContentPosition) {
        ContentPosition.TOP -> template.textContentY
        ContentPosition.BOTTOM -> template.textContentY + (template.textContentHeight - paragraphHeight)
        else -> containerCentreY - (paragraphHeight / 2)
    }

    val textCentreX = template.textContentX + (template.textContentWidth / 2)
    val textCentreY = template.textContentY + (template.textContentHeight / 2)

    return TextDrawData(
        paragraph,
        textX,
        textY,
        textCentreX,
        textCentreY,
        resizedFont
    )
}

suspend fun drawText(
    image: BufferedImage,
    textDrawData: TextDrawData,
    timestamp: Duration,
    template: Template,
): BufferedImage {
    val imageWithText = image.copySize()
    val graphics = imageWithText.createGraphics()

    val contentClip = template.getContentClip()
    template.fill?.let {
        graphics.color = it
        if (contentClip == null)
            graphics.fillRect(0, 0, imageWithText.width, imageWithText.height)
        else
            graphics.fill(contentClip)
    }

    if (template.isBackground)
        graphics.drawImage(image, 0, 0, null)

    val font = textDrawData.font
    graphics.font = font
    graphics.configureTextDrawQuality()
    graphics.color = template.textColor

    contentClip?.let {
        graphics.clip = it
    }

    val text = textDrawData.text
    val textX = textDrawData.textX
    val textY = textDrawData.textY
    val rotationRadians = template.contentRotationRadians
    if (rotationRadians != 0.0)
        graphics.rotate(
            rotationRadians,
            textDrawData.textCentreX.toDouble(),
            textDrawData.textCentreY.toDouble()
        )

    text.draw(graphics, textX, textY, timestamp)

    if (rotationRadians != 0.0)
        graphics.rotate(
            -rotationRadians,
            textDrawData.textCentreX.toDouble(),
            textDrawData.textCentreY.toDouble()
        )

    if (contentClip != null)
        graphics.clip = null

    if (!template.isBackground)
        graphics.drawImage(image, 0, 0, null)

    graphics.dispose()
    return imageWithText
}

fun BufferedImage.makeRoundedCorners(cornerRadius: Double): BufferedImage {
    val output = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val graphics = output.createGraphics()
    /*
    This is what we want, but it only does hard-clipping, i.e. aliasing
    graphics.setClip(new RoundRectangle2D ...)
    So instead fake soft-clipping by first drawing the desired clip shape
    in fully opaque white with antialiasing enabled...
     */
    graphics.composite = AlphaComposite.Src
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.color = Color.WHITE
    graphics.fill(
        RoundRectangle2D.Double(
            0.0,
            0.0,
            width.toDouble(),
            height.toDouble(),
            cornerRadius,
            cornerRadius,
        )
    )
    /*
    ...then compositing the image on top,
    using the white shape from above as alpha source
     */
    graphics.composite = AlphaComposite.SrcAtop
    graphics.drawImage(this, 0, 0, null)
    graphics.dispose()
    return output
}
