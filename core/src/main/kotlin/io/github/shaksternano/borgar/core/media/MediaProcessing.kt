package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.io.NamedFile
import io.github.shaksternano.borgar.core.io.createTemporaryFile
import io.github.shaksternano.borgar.core.io.useAll
import io.github.shaksternano.borgar.core.media.reader.MediaReader
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
    val modifyImageReader: (MediaReader<ImageFrame>) -> MediaReader<ImageFrame>,
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
    input: Path,
    config: MediaProcessConfig,
    maxFileSize: Long,
): NamedFile {
    val inputFormat = mediaFormat(input) ?: input.extension
    val inputFile = input.toFile()
    val (imageReader, audioReader) = withContext(Dispatchers.IO) {
        MediaReaders.createImageReader(inputFile, inputFormat) to MediaReaders.createAudioReader(inputFile, inputFormat)
    }
    val outputFormat = config.outputFormat(inputFormat)
    val output = processMedia(
        config.modifyImageReader(imageReader),
        audioReader,
        createTemporaryFile(config.outputName, outputFormat),
        outputFormat,
        config.processor,
        maxFileSize
    )
    return NamedFile(
        output,
        config.outputName,
        outputFormat
    )
}

suspend fun <T : Any> processMedia(
    imageReader: MediaReader<ImageFrame>,
    audioReader: MediaReader<AudioFrame>,
    output: Path,
    outputFormat: String,
    processor: ImageProcessor<T>,
    maxFileSize: Long,
): Path {
    val newImageReader: MediaReader<ImageFrame>
    val newAudioReader: MediaReader<AudioFrame>
    if (processor.speed < 0) {
        newImageReader = imageReader.reversed()
        newAudioReader = audioReader.reversed()
    } else {
        newImageReader = imageReader
        newAudioReader = audioReader
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
                    MediaWriters.createWriter(
                        output.toFile(),
                        outputFormat,
                        newImageReader.loopCount(),
                        newAudioReader.audioChannels(),
                        newAudioReader.audioSampleRate(),
                        newAudioReader.audioBitrate(),
                        maxFileSize,
                        newImageReader.duration()
                    )
                }
            ) { imageIterator, audioIterator, writer ->
                lateinit var constantFrameDataValue: T
                var constantDataSet = false
                while (imageIterator.hasNext()) {
                    val imageFrame = imageIterator.next()
                    if (!constantDataSet) {
                        constantFrameDataValue = processor.constantData(imageFrame.content)
                        constantDataSet = true
                    }
                    writer.writeImageFrame(
                        imageFrame.transform(
                            ImageUtil.resize(
                                processor.transformImage(imageFrame, constantFrameDataValue),
                                resizeRatio
                            ),
                            processor.absoluteSpeed
                        )
                    )
                    if (writer.isStatic()) {
                        break
                    }
                }
                if (writer.supportsAudio()) {
                    while (audioIterator.hasNext()) {
                        val audioFrame = audioIterator.next()
                        writer.writeAudioFrame(audioFrame.transform(processor.absoluteSpeed))
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
