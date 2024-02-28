package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.reader.first
import io.github.shaksternano.borgar.core.media.reader.transform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.math.min

interface MediaProcessingConfig {

    val outputName: String?

    suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader = imageReader

    suspend fun transformAudioReader(audioReader: AudioReader, outputFormat: String): AudioReader = audioReader

    fun transformOutputFormat(inputFormat: String): String = inputFormat

    infix fun then(after: MediaProcessingConfig): MediaProcessingConfig = object : MediaProcessingConfig {

        override val outputName: String? = after.outputName ?: this@MediaProcessingConfig.outputName

        override suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader {
            val firstReader = this@MediaProcessingConfig.transformImageReader(imageReader, outputFormat)
            return after.transformImageReader(firstReader, outputFormat)
        }

        override suspend fun transformAudioReader(audioReader: AudioReader, outputFormat: String): AudioReader {
            val firstReader = this@MediaProcessingConfig.transformAudioReader(audioReader, outputFormat)
            return after.transformAudioReader(firstReader, outputFormat)
        }

        override fun transformOutputFormat(inputFormat: String): String {
            val firstFormat = this@MediaProcessingConfig.transformOutputFormat(inputFormat)
            return after.transformOutputFormat(firstFormat)
        }
    }
}

class SimpleMediaProcessingConfig(
    private val processor: ImageProcessor<*>,
    override val outputName: String?,
) : MediaProcessingConfig {

    constructor(
        outputName: String?,
        transform: (ImageFrame) -> BufferedImage,
    ) : this(
        SimpleImageProcessor(transform),
        outputName,
    )

    override suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader =
        imageReader.transform(processor, outputFormat)
}

suspend fun processMedia(
    input: DataSource,
    config: MediaProcessingConfig,
    maxFileSize: Long,
): FileDataSource {
    val isTempFile = input.path == null
    val fileInput = input.getOrWriteFile()
    val path = fileInput.path
    return try {
        val inputFormat = mediaFormat(path) ?: path.extension
        val imageReader = createImageReader(fileInput, inputFormat)
        val audioReader = createAudioReader(fileInput, inputFormat)
        val outputFormat = config.transformOutputFormat(inputFormat)
        val outputName = config.outputName ?: fileInput.filenameWithoutExtension()
        val output = processMedia(
            config.transformImageReader(imageReader, outputFormat),
            config.transformAudioReader(audioReader, outputFormat),
            createTemporaryFile(outputName, outputFormat),
            outputFormat,
            maxFileSize,
        )
        val filename = filename(outputName, outputFormat)
        DataSource.fromFile(
            output,
            filename,
        )
    } finally {
        if (isTempFile)
            path.deleteSilently()
    }
}

suspend fun processMedia(
    imageReader: ImageReader,
    audioReader: AudioReader,
    output: Path,
    outputFormat: String,
    maxFileSize: Long,
): Path {
    return useAllIgnored(imageReader, audioReader) {
        var outputSize: Long
        var resizeRatio = 1.0
        val maxResizeAttempts = 3
        var attempts = 0
        do {
            createWriter(
                output,
                outputFormat,
                imageReader.loopCount,
                audioReader.audioChannels,
                audioReader.audioSampleRate,
                audioReader.audioBitrate,
                maxFileSize,
                imageReader.duration,
            ).use { writer ->
                val imageFlow = if (writer.isStatic) {
                    flowOf(imageReader.first())
                } else {
                    imageReader.asFlow()
                }
                imageFlow.collect { imageFrame ->
                    writer.writeImageFrame(
                        imageFrame.copy(
                            content = imageFrame.content.resize(resizeRatio),
                        )
                    )
                }
                if (writer.supportsAudio) {
                    audioReader.asFlow().collect(writer::writeAudioFrame)
                }
            }
            outputSize = withContext(Dispatchers.IO) {
                output.fileSize()
            }
            resizeRatio = min((maxFileSize.toDouble() / outputSize), 0.9)
            attempts++
        } while (maxFileSize in 1..<outputSize && attempts < maxResizeAttempts)
        output
    }
}
