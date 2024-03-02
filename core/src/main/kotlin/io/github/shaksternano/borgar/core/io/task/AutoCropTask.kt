package io.github.shaksternano.borgar.core.io.task

import com.sksamuel.scrimage.AutocropOps
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.pixels.PixelsExtractor
import java.awt.Color
import java.awt.Rectangle
import java.awt.image.BufferedImage

class AutoCropTask(
    private val cropColor: Color?,
    colorTolerance: Double,
    onlyCheckFirst: Boolean,
    maxFileSize: Long,
) : FindCropTask(
    onlyCheckFirst,
    "cropped",
    maxFileSize,
    "Couldn't find area to crop!",
) {

    private val colorTolerance: Int = (colorTolerance * 255).toInt()

    override fun findCropArea(image: BufferedImage): Rectangle {
        val newCropColor = cropColor ?: Color(image.getRGB(0, 0), true)
        val width = image.width
        val height = image.height
        val extractor = PixelsExtractor { rectangle ->
            ImmutableImage.wrapAwt(image).pixels(
                rectangle.x,
                rectangle.y,
                rectangle.width,
                rectangle.height
            )
        }
        val x1 = AutocropOps.scanright(newCropColor, height, width, 0, extractor, colorTolerance)
        val x2 = AutocropOps.scanleft(newCropColor, height, width - 1, extractor, colorTolerance)
        val y1 = AutocropOps.scandown(newCropColor, height, width, 0, extractor, colorTolerance)
        val y2 = AutocropOps.scanup(newCropColor, width, height - 1, extractor, colorTolerance)
        return if (x1 == 0 && y1 == 0 && x2 == width - 1 && y2 == height - 1) {
            Rectangle(0, 0, width, height)
        } else {
            Rectangle(x1, y1, x2 - x1, y2 - y1)
        }
    }
}
