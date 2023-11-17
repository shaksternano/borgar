package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.exception.UnreadableFileException
import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.reader.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
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
    val isTempFile = input.path == null
    val fileInput = input.getOrWriteFile()
    val path = fileInput.path
    return try {
        val inputFormat = mediaFormat(path) ?: path.extension
        val (imageReader, audioReader) = try {
            withContext(Dispatchers.IO) {
                createImageReader(fileInput, inputFormat) to createAudioReader(fileInput, inputFormat)
            }
        } catch (t: Throwable) {
            throw UnreadableFileException(t)
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
        DataSource.fromFile(
            output,
            filename,
        )
    } finally {
        if (isTempFile) {
            path.deleteSilently()
        }
    }
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
    return useAllIgnored(newImageReader, newAudioReader, processor) {
        var outputSize: Long
        var resizeRatio = 1F
        val maxResizeAttempts = 3
        var attempts = 0
        do {
            createWriter(
                output,
                outputFormat,
                newImageReader.loopCount,
                newAudioReader.audioChannels,
                newAudioReader.audioSampleRate,
                newAudioReader.audioBitrate,
                maxFileSize,
                newImageReader.duration,
            ).use { writer ->
                val imageFlow = if (writer.isStatic) {
                    flowOf(newImageReader.first())
                } else {
                    newImageReader.asFlow()
                }
                lateinit var constantFrameDataValue: T
                var constantDataSet = false
                imageFlow.collect { imageFrame ->
                    if (imageFrame.content is DualBufferedImage && processor is DualImageProcessor<T>) {
                        processor.frame2 = imageFrame.copy(
                            content = imageFrame.content.second
                        )
                    }
                    if (!constantDataSet) {
                        constantFrameDataValue = processor.constantData(imageFrame.content)
                        constantDataSet = true
                    }
                    writer.writeImageFrame(
                        imageFrame.copy(
                            content = processor.transformImage(imageFrame, constantFrameDataValue).resize(resizeRatio),
                            duration = imageFrame.duration / processor.absoluteSpeed.toDouble()
                        )
                    )
                }

                if (writer.supportsAudio) {
                    newAudioReader.asFlow().collect { audioFrame ->
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
        output
    }
}
