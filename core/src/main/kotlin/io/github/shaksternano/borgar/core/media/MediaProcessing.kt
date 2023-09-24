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

class MediaProcessConfig(
    val processor: ImageProcessor<out Any>,
    val outputName: String,
    val outputFormat: (String) -> String = { it },
    val modifyImageReader: (ImageReader) -> ImageReader = { it },
) {

    infix fun then(after: MediaProcessConfig): MediaProcessConfig {
        return MediaProcessConfig(
            processor then after.processor,
            after.outputName,
            {
                val firstFormat = outputFormat(it)
                after.outputFormat(firstFormat)
            }
        ) {
            val firstReader = modifyImageReader(it)
            after.modifyImageReader(firstReader)
        }
    }
}

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
    val outputFormat = config.outputFormat(inputFormat)
    val output = processMedia(
        config.modifyImageReader(imageReader),
        audioReader,
        createTemporaryFile(config.outputName, outputFormat),
        outputFormat,
        config.processor,
        maxFileSize,
    )
    val filename = filename(config.outputName, outputFormat)
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
        newImageReader.start()
        newAudioReader.start()
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
                        newImageReader.duration
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
                            duration = imageFrame.duration / processor.absoluteSpeed
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
                                duration = audioFrame.duration / processor.absoluteSpeed
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
