package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.media.distanceTo
import io.github.shaksternano.borgar.core.media.isGreyScale
import java.awt.Color
import java.awt.Rectangle
import java.awt.image.BufferedImage

class UncaptionTask(
    onlyCheckFirst: Boolean,
    maxFileSize: Long,
) : FindCropTask(
    onlyCheckFirst,
    "uncaptioned",
    maxFileSize,
    "Couldn't detect caption!",
) {

    override fun findCropArea(image: BufferedImage): Rectangle {
        val fullImageArea = Rectangle(0, 0, image.width, image.height)
        val nonTopCaptionArea = findNonCaptionArea(image, true)
        val nonTopColoredCaptionArea = findNonCaptionAreaColored(image, true)
        val nonBottomCaptionArea = findNonCaptionArea(image, false)
        val nonBottomColoredCaptionArea = findNonCaptionAreaColored(image, false)
        return fullImageArea
            .intersection(nonTopCaptionArea)
            .intersection(nonTopColoredCaptionArea)
            .intersection(nonBottomCaptionArea)
            .intersection(nonBottomColoredCaptionArea)
    }

    private fun findNonCaptionArea(image: BufferedImage, topCaption: Boolean): Rectangle {
        var continueLooking = true
        var captionEnd = -1
        var y =
            if (topCaption) 0
            else image.height - 1
        while (if (topCaption) y < image.height else y >= 0) {
            for (x in 0 until image.width) {
                val color = Color(image.getRGB(x, y))
                if (!color.isGreyScale) {
                    continueLooking = false
                    break
                }
            }
            if (continueLooking) {
                captionEnd = y

                if (topCaption) {
                    y++
                } else {
                    y--
                }
            } else {
                break
            }
        }
        return createNonCaptionArea(image, topCaption, captionEnd)
    }

    private fun findNonCaptionAreaColored(image: BufferedImage, topCaption: Boolean): Rectangle {
        var continueLooking = true
        var captionEnd = -1
        var y =
            if (topCaption) 0
            else image.height - 1
        val colorTolerance = 150
        while (if (topCaption) y < image.height else y >= 0) {
            var rowIsCompletelyWhite = true
            for (x in 0 until image.width) {
                val color = Color(image.getRGB(x, y))
                val colorDistance = color distanceTo Color.WHITE
                if (colorDistance > colorTolerance) {
                    rowIsCompletelyWhite = false
                    if ((if (topCaption) y == 0 else y == image.height - 1)
                        || (x == 0)
                        || (x == image.width - 1)
                    ) {
                        continueLooking = false
                        break
                    }
                } else if (rowIsCompletelyWhite && x == image.width - 1) {
                    captionEnd = y
                }
            }
            if (continueLooking) {
                if (topCaption) {
                    y++
                } else {
                    y--
                }
            } else {
                break
            }
        }
        return createNonCaptionArea(image, topCaption, captionEnd)
    }

    private fun createNonCaptionArea(image: BufferedImage, topCaption: Boolean, captionEnd: Int): Rectangle {
        if (captionEnd != -1) {
            val width = image.width
            val height =
                if (topCaption) image.height - captionEnd - 1
                else captionEnd
            if (width > 0 && height > 0) {
                return if (topCaption) {
                    Rectangle(0, captionEnd + 1, width, height)
                } else {
                    Rectangle(0, 0, width, height)
                }
            }
        }
        return Rectangle(0, 0, image.width, image.height)
    }
}
