package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.cropMedia
import java.awt.Rectangle
import java.awt.image.BufferedImage

abstract class FindCropTask(
    private val onlyCheckFirst: Boolean,
    private val outputName: String,
    private val maxFileSize: Long,
    private val failureMessage: String,
) : MappedFileTask(true) {

    protected abstract fun findCropArea(image: BufferedImage): Rectangle

    final override suspend fun process(input: DataSource): DataSource = cropMedia(
        input,
        onlyCheckFirst,
        outputName,
        maxFileSize,
        failureMessage,
        ::findCropArea,
    )

    override fun then(after: FileTask): FileTask {
        return if (after is FindCropTask) {
            object : FindCropTask(
                onlyCheckFirst,
                outputName,
                maxFileSize,
                failureMessage,
            ) {
                override fun findCropArea(image: BufferedImage): Rectangle {
                    val firstCrop = this@FindCropTask.findCropArea(image)
                    val secondCrop = after.findCropArea(image)
                    return firstCrop.union(secondCrop)
                }
            }
        } else {
            super.then(after)
        }
    }
}
