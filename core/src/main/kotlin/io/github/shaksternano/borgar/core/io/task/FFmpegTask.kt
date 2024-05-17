package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.collect.indicesOf
import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.getEnvVar
import io.github.shaksternano.borgar.core.util.splitWords
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.bytedeco.ffmpeg.ffmpeg
import org.bytedeco.javacpp.Loader
import java.io.InputStreamReader
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString


private val FFMPEG_PATH: String = getEnvVar("FFMPEG_PATH") ?: Loader.load(ffmpeg::class.java)

class FFmpegTask(
    private val arguments: String,
) : MappedFileTask() {

    override suspend fun process(input: DataSource): DataSource = input.useFile { fileInput ->
        val inputPath = fileInput.path
        val splitArguments = arguments.splitWords()
        val inputIndices = splitArguments.indicesOf("-i")
        val ffmpegArguments = splitArguments.filterIndexed { index, _ ->
            (index !in inputIndices && index - 1 !in inputIndices)
        }.ifEmpty {
            listOf(inputPath.toString())
        }
        val output = ffmpegArguments.last()
        if (fileExtension(output).isBlank()) {
            throw ErrorResponseException("Output filename must have an extension!")
        }
        val outputFilename = Path(output).filename
        val outputPath = getTemporaryFile(outputFilename)
        outputPath.toFile().deleteOnExit()
        val ffmpegCommand = listOf(FFMPEG_PATH, "-i", inputPath.absolutePathString()) +
            ffmpegArguments.subList(0, ffmpegArguments.lastIndex) +
            outputPath.absolutePathString()
        val processBuilder = ProcessBuilder(ffmpegCommand)
        withContext(Dispatchers.IO) {
            val process = processBuilder.start()
            val ffmpegErrorDeferred = async {
                val lines = InputStreamReader(process.errorStream).readLines()
                lines.joinToString("\n")
            }
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                var errorMessage = "FFmpeg command failed with exit code $exitCode"
                val ffmpegError = ffmpegErrorDeferred.await()
                if (ffmpegError.isNotBlank()) {
                    errorMessage += ". FFmpeg error:\n$ffmpegError"
                }
                logger.error(errorMessage)
                throw ErrorResponseException("FFmpeg command failed!")
            }
        }
        DataSource.fromFile(outputPath, outputFilename)
    }
}
