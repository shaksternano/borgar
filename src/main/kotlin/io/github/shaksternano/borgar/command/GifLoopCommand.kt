package io.github.shaksternano.borgar.command

import com.google.common.collect.ListMultimap
import com.google.common.io.Files
import io.github.shaksternano.borgar.Main
import io.github.shaksternano.borgar.command.util.CommandResponse
import io.github.shaksternano.borgar.util.MessageUtil
import io.github.shaksternano.borgar.util.io.IndexedInputStream
import io.github.shaksternano.borgar.util.io.createTemporaryFile
import io.github.shaksternano.borgar.util.io.indexed
import io.github.shaksternano.borgar.util.io.modifiable
import io.github.shaksternano.borgar.util.tenor.TenorMediaType
import io.github.shaksternano.borgar.util.tenor.TenorUtil
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.EOFException
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.appendBytes
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.use
import kotlin.jvm.optionals.getOrElse
import kotlin.math.pow

object GifLoopCommand : KotlinCommand<Path>(
    "loop",
    "Changes the number of times a GIF loops. Required arguments: [" +
            "The number of times to loop the GIF. " +
            "In other words, the loop count + 1 is the number of times the GIF will play. " +
            "Use 0 to remove looping. " +
            "Use -1 to make the GIF loop forever." +
            "]"
) {

    override suspend fun executeSuspend(
        arguments: List<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): CommandResponse<Path> {
        val loopCountString = arguments.firstOrNull() ?: return CommandResponse("No loop count specified!")
        val loopCount = loopCountString.toIntOrNull()
            ?.let {
                when (it) {
                    // Remove looping
                    0 -> -1
                    // Loop forever
                    -1 -> 0
                    else -> it
                }
            }
            ?: return CommandResponse("Loop count is not a number!")
        if (loopCount !in -1..65535) {
            return CommandResponse("Loop count must be between -1 and 65535 inclusive!")
        }
        HttpClient(CIO).use { client ->
            val url = MessageUtil.getUrl(event.message)
                .await()
                .getOrElse {
                    return CommandResponse("No media found!")
                }
                .let {
                    try {
                        TenorUtil.retrieveTenorMediaUrl(it, TenorMediaType.GIF_NORMAL, Main.getTenorApiKey()).await()
                    } catch (e: IllegalArgumentException) {
                        it
                    }
                }
            val response = client.get(url)
            val contentLength = response.headers["Content-Length"]?.toLongOrNull() ?: 0
            if (contentLength > Message.MAX_FILE_SIZE) {
                return CommandResponse("File is too large!")
            }
            val urlNoQueryParams = url.split('?').first()
            val fileNameWithoutExtension = Files.getNameWithoutExtension(urlNoQueryParams)
            val extension = response.headers["Content-Type"]
                ?.split("/")
                ?.getOrNull(1)
                ?: Files.getFileExtension(urlNoQueryParams)
            val path = createTemporaryFile(fileNameWithoutExtension, extension)
            download(response, path)
            val gifInfo = try {
                locateGifComponents(path.inputStream())
            } catch (e: IllegalArgumentException) {
                Main.getLogger().error("Error reading GIF file", e)
                return CommandResponse("Not a GIF file!")
            }
            val fileName = if (extension.isBlank()) fileNameWithoutExtension else "$fileNameWithoutExtension.$extension"
            // Do not add the application extension if no looping is wanted.
            val applicationExtension = if (loopCount < 0) listOf() else createApplicationExtension(loopCount)
            val inputStream = path.inputStream().buffered().modifiable()
            inputStream.insertBytes(gifInfo.globalColorTableEnd, applicationExtension)
            gifInfo.applicationExtensions.forEach {
                inputStream.removeBytes(it.first, it.last - it.first + 1)
            }
            return CommandResponse<Path>(inputStream, fileName).withResponseData(path)
        }
    }

    override fun handleFirstResponse(response: Message, event: MessageReceivedEvent, responseData: Path?) {
        responseData?.deleteIfExists()
    }

    private suspend fun download(response: HttpResponse, path: Path) {
        val channel = response.bodyAsChannel()
        while (!channel.isClosedForRead) {
            val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
            while (!packet.isEmpty) {
                val bytes = packet.readBytes()
                path.appendBytes(bytes)
            }
        }
    }

    /*
    GIF specification:
    https://giflib.sourceforge.net/whatsinagif/bits_and_bytes.html
     */
    private fun locateGifComponents(inputStream: InputStream): GifInfo {
        inputStream.buffered().indexed().use {
            try {
                readHeader(it)
                val globalColorTableBytes = readLogicalScreenDescriptor(it)
                readColorTable(it, globalColorTableBytes)
                val endOfGlobalColorTable = it.nextIndex
                val applicationExtensions = readContent(it)
                return GifInfo(endOfGlobalColorTable, applicationExtensions)
            } catch (e: EOFException) {
                throw IllegalArgumentException("Reached end of stream", e)
            }
        }
    }

    private fun readHeader(inputStream: InputStream) {
        val bytes = inputStream.readNBytes(6)
        // Should either be GIF87a or GIF89a
        val id = bytes.decodeToString()
        if (!id.startsWith("GIF")) {
            throw IllegalArgumentException("File doesn't start with GIF header")
        }
    }

    private fun readLogicalScreenDescriptor(inputStream: InputStream): Long {
        /*
        Bytes:
        1-2 : canvas width
        3-4 : canvas height
         */
        inputStream.skipNBytes(4)

        /*
        Bits:
        1   : global color table flag
        2-4 : color resolution
        5   : global color table sort flag
        6-8 : global color table size
         */
        val packed = inputStream.read()
        val globalColorTableFlag = packed and 128 != 0
        val globalColorTableSize = packed and 7

        /*
        Bytes:
        1 : background color index
        2 : pixel aspect ratio
         */
        inputStream.skipNBytes(2)

        return if (globalColorTableFlag) {
            calculateColorTableBytes(globalColorTableSize)
        } else {
            0
        }
    }

    private fun calculateColorTableBytes(colorTableSize: Int): Long {
        /*
        Color table bytes = 2^(N+1) * 3:
        2^(N+1) colors, where N the color table size
        3 bytes per color
         */
        return 2.0.pow(colorTableSize + 1).toLong() * 3
    }

    private fun readColorTable(inputStream: InputStream, size: Long) {
        inputStream.skipNBytes(size)
    }

    private fun readContent(inputStream: IndexedInputStream): List<LongRange> {
        /*
        Some GIFs have more than one application extension, which is invalid.
        These will all be removed.
         */
        val applicationExtensions = mutableListOf<LongRange>()
        // read GIF file content blocks
        var continueReading = true
        while (continueReading) {
            // Code
            when (inputStream.read()) {
                // Extension
                0x21 -> readExtension(inputStream)?.let { applicationExtensions.add(it) }
                // Image descriptor
                0x2C -> readImage(inputStream)
                // Terminator
                0x3B -> continueReading = false
                // End of stream
                -1 -> continueReading = false
            }
        }
        return applicationExtensions
    }

    private fun readExtension(inputStream: IndexedInputStream): LongRange? {
        when (inputStream.read()) {
            // Application extension
            0xFF -> {
                val start = inputStream.nextIndex - 2
                skipBlocks(inputStream)
                val endExclusive = inputStream.nextIndex
                return start until endExclusive
            }

            else -> skipBlocks(inputStream)
        }
        return null
    }

    private fun readImage(inputStream: InputStream) {
        val localColorTableBytes = readImageDescriptor(inputStream)
        readColorTable(inputStream, localColorTableBytes)
        readImageData(inputStream)
    }

    private fun readImageDescriptor(inputStream: InputStream): Long {
        /*
        Bytes:
        1-2 : image left position
        3-4 : image top position
        5-6 : image width
        7-8 : image height
         */
        inputStream.skipNBytes(8)
        /*
        Bits:
        1   : local color table flag
        2   : interlace flag
        3   : sort flag
        4-5 : reserved for future use
        6-8 : local color table size
         */
        val packed = inputStream.read()
        val localColorTableFlag = packed and 128 != 0
        val localColorTableSize = packed and 7
        return if (localColorTableFlag) {
            calculateColorTableBytes(localColorTableSize)
        } else {
            0
        }
    }

    private fun readImageData(inputStream: InputStream) {
        // LZW minimum code size
        inputStream.skipNBytes(1)
        skipBlocks(inputStream)
    }

    private fun skipBlocks(inputStream: InputStream) {
        var blockSize: Int
        do {
            blockSize = skipBlock(inputStream)
        } while (blockSize > 0)
    }

    private fun skipBlock(inputStream: InputStream): Int {
        val blockSize = inputStream.read()
        inputStream.skipNBytes(blockSize.toLong())
        return blockSize
    }

    private fun createApplicationExtension(loopCount: Int): List<Int> {
        val applicationExtension = mutableListOf<Int>()
        applicationExtension.add(0x21)                              // Extension code
        applicationExtension.add(0xFF)                              // Application extension label
        applicationExtension.add(0x0B)                              // Length of Application block
        "NETSCAPE2.0".forEach { applicationExtension.add(it.code) } // Application identifier
        applicationExtension.add(0x03)                              // Length of data sub-block
        applicationExtension.add(0x01)                              // Constant
        applicationExtension.add(loopCount and 0xFF)                // Loop count in two byte little-endian format.
        applicationExtension.add((loopCount shr 8) and 0xFF)
        applicationExtension.add(0x00)                              // Data Sub-Block Terminator
        return applicationExtension
    }

    private data class GifInfo(
        val globalColorTableEnd: Long,
        val applicationExtensions: List<LongRange>,
    )
}
