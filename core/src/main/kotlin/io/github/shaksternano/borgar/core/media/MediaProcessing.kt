package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.reader.ZippedImageReaderIterator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.math.min

interface MediaProcessConfig {

    val processor: ImageProcessor<out Any>
    val outputName: String?

    fun transformOutputFormat(inputFormat: String): String = inputFormat

    fun transformImageReader(imageReader: ImageReader): ImageReader = imageReader

    infix fun then(after: MediaProcessConfig): MediaProcessConfig = object : MediaProcessConfig {
        override val processor: ImageProcessor<out Any> = this@MediaProcessConfig.processor then after.processor
        override val outputName: String? = after.outputName ?: this@MediaProcessConfig.outputName

        override fun transformOutputFormat(inputFormat: String): String {
            val firstFormat = this@MediaProcessConfig.transformOutputFormat(inputFormat)
            return after.transformOutputFormat(firstFormat)
        }

        override fun transformImageReader(imageReader: ImageReader): ImageReader {
            val firstReader = this@MediaProcessConfig.transformImageReader(imageReader)
            return after.transformImageReader(firstReader)
        }
    }
}

class BasicMediaProcessConfig(
    override val processor: ImageProcessor<out Any>,
    override val outputName: String?,
) : MediaProcessConfig

suspend fun processMedia(
    input: DataSource,
    config: MediaProcessConfig,
    maxFileSize: Long,
): FileDataSource {
    val fileInput = input.getOrWriteFile()
    val path = fileInput.path
    val inputFormat = mediaFormat(path) ?: path.extension
    val (imageReader, audioReader) = withContext(Dispatchers.IO) {
        createImageReader(fileInput, inputFormat) to createAudioReader(fileInput, inputFormat)
    }
    val outputFormat = config.transformOutputFormat(inputFormat)
    val outputName = config.outputName ?: fileInput.filenameWithoutExtension()
    val output = processMedia(
        config.transformImageReader(imageReader),
        audioReader,
        createTemporaryFile(outputName, outputFormat),
        outputFormat,
        config.processor,
        maxFileSize,
    )
    val filename = filename(outputName, outputFormat)
    return DataSource.fromFile(
        output,
        filename,
    )
}

suspend fun <T : Any> processMedia(
    imageReader: ImageReader,
    audioReader: AudioReader,
    output: Path,
    outputFormat: String,
    processor: ImageProcessor<T>,
    maxFileSize: Long,
): Path {
    val (newImageReader, newAudioReader) = if (processor.speed < 0) {
        imageReader.reversed to audioReader.reversed
    } else {
        imageReader to audioReader
    }
    useAll(newImageReader, newAudioReader, processor) { _, _, _ ->
        var outputSize: Long
        var resizeRatio = 1F
        val maxResizeAttempts = 3
        var attempts = 0
        do {
            useAll(
                newImageReader.iterator(),
                newAudioReader.iterator(),
                withContext(Dispatchers.IO) {
                    createWriter(
                        output,
                        outputFormat,
                        newImageReader.loopCount,
                        newAudioReader.audioChannels,
                        newAudioReader.audioSampleRate,
                        newAudioReader.audioBitrate,
                        maxFileSize,
                        newImageReader.duration,
                    )
                }
            ) { imageIterator, audioIterator, writer ->
                lateinit var constantFrameDataValue: T
                var constantDataSet = false
                while (imageIterator.hasNext()) {
                    val imageFrame = imageIterator.next()
                    if (imageIterator is ZippedImageReaderIterator && processor is DualImageProcessor<T>) {
                        processor.frame2 = imageIterator.next2()
                    }
                    if (!constantDataSet) {
                        constantFrameDataValue = processor.constantData(imageFrame.content)
                        constantDataSet = true
                    }
                    writer.writeImageFrame(
                        imageFrame.copy(
                            content = ImageUtil.resize(
                                processor.transformImage(imageFrame, constantFrameDataValue),
                                resizeRatio
                            ),
                            duration = imageFrame.duration / processor.absoluteSpeed.toDouble()
                        )
                    )
                    if (writer.isStatic) {
                        break
                    }
                }
                if (writer.supportsAudio) {
                    while (audioIterator.hasNext()) {
                        val audioFrame = audioIterator.next()
                        writer.writeAudioFrame(
                            audioFrame.copy(
                                duration = audioFrame.duration / processor.absoluteSpeed.toDouble()
                            )
                        )
                    }
                }
            }
            outputSize = withContext(Dispatchers.IO) {
                output.fileSize()
            }
            resizeRatio = min((maxFileSize.toDouble() / outputSize), 0.9).toFloat()
            attempts++
        } while (maxFileSize in 1..<outputSize && attempts < maxResizeAttempts)
        return output
    }
}
