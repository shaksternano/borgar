package io.github.shaksternano.borgar.core.task

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.pixels.Pixel
import com.sksamuel.scrimage.pixels.PixelTools
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    maxFileSize,
    "Couldn't find area to crop!",
) {

    private val colorTolerance: Int = (colorTolerance * 255).toInt()

    override suspend fun findCropArea(image: BufferedImage): Rectangle = coroutineScope {
        val newCropColor = cropColor ?: Color(image.getRGB(0, 0), true)

        val x1Deferred = async {
            scanRight(image, newCropColor, colorTolerance)
        }
        val x2Deferred = async {
            scanLeft(image, newCropColor, colorTolerance)
        }
        val y1Deferred = async {
            scanDown(image, newCropColor, colorTolerance)
        }
        val y2Deferred = async {
            scanUp(image, newCropColor, colorTolerance)
        }

        val x1 = x1Deferred.await()
        val x2 = x2Deferred.await()
        val y1 = y1Deferred.await()
        val y2 = y2Deferred.await()

        val width = image.width
        val height = image.height
        if (x1 == 0 && y1 == 0 && x2 == width - 1 && y2 == height - 1) {
            Rectangle(0, 0, width, height)
        } else {
            Rectangle(x1, y1, x2 - x1, y2 - y1)
        }
    }

    // Faster versions of the functions from Scrimage
    private fun scanRight(
        image: BufferedImage,
        color: Color,
        tolerance: Int,
    ): Int {
        val width = image.width
        val height = image.height
        var column = 0
        val rectangle = Rectangle(column, 0, 1, height)
        while (column < width && PixelTools.colorMatches(color, tolerance, extractPixels(image, rectangle))) {
            rectangle.setBounds(++column, 0, 1, height)
        }
        return column
    }

    private fun scanLeft(
        image: BufferedImage,
        color: Color,
        tolerance: Int,
    ): Int {
        val width = image.width
        val height = image.height
        var column = width - 1
        val rectangle = Rectangle(column, 0, 1, height)
        while (column > 0 && PixelTools.colorMatches(color, tolerance, extractPixels(image, rectangle))) {
            rectangle.setBounds(--column, 0, 1, height)
        }
        return column
    }

    private fun scanDown(
        image: BufferedImage,
        color: Color,
        tolerance: Int,
    ): Int {
        val width = image.width
        val height = image.height
        var row = 0
        val rectangle = Rectangle(0, row, width, 1)
        while (row < height && PixelTools.colorMatches(color, tolerance, extractPixels(image, rectangle))) {
            rectangle.setBounds(0, ++row, width, 1)
        }
        return row
    }

    private fun scanUp(
        image: BufferedImage,
        color: Color,
        tolerance: Int,
    ): Int {
        val width = image.width
        val height = image.height
        var row = height - 1
        val rectangle = Rectangle(0, row, width, 1)
        while (row > 0 && PixelTools.colorMatches(color, tolerance, extractPixels(image, rectangle))) {
            rectangle.setBounds(0, --row, width, 1)
        }
        return row
    }

    private fun extractPixels(image: BufferedImage, rectangle: Rectangle): Array<Pixel> =
        ImmutableImage.wrapAwt(image).pixels(
            rectangle.x,
            rectangle.y,
            rectangle.width,
            rectangle.height,
        )
}
