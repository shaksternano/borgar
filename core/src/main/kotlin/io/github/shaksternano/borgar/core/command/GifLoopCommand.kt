package io.github.shaksternano.borgar.core.command

import com.google.common.collect.ListMultimap
import com.google.common.io.Files
import io.github.shaksternano.borgar.core.Main
import io.github.shaksternano.borgar.core.command.util.CommandResponse
import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.util.MessageUtil
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
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
        val loopCountString = arguments.firstOrNull() ?: return CommandResponse(
            "No loop count specified!"
        )
        val loopCount = loopCountString.toIntOrNull()
            ?.let {
                // Swap 0 and -1
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
        val url = MessageUtil.getUrlTenor(event.message)
            .await()
            .getOrElse {
                return CommandResponse("No media found!")
            }
        HttpClient(CIO).use { client ->
            val response = client.get(url)
            val contentLength = response.contentLength() ?: 0
            if (contentLength > Message.MAX_FILE_SIZE) {
                return CommandResponse("File is too large!")
            }
            val urlNoQueryParams = url.split('?').first()
            val fileNameWithoutExtension = Files.getNameWithoutExtension(urlNoQueryParams)
            val extension = response.contentType()
                ?.contentSubtype
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
            // Do not add the application extension if no looping is wanted
            val applicationExtension = if (loopCount < 0) listOf() else createApplicationExtension(loopCount)
            val inputStream = path.inputStream().buffered().modifiable()
            // Insert the new application extension with the specified loop count after the global color table
            inputStream.insertBytes(gifInfo.globalColorTableEnd, applicationExtension)
            // Remove all existing application extensions
            gifInfo.applicationExtensions.forEach {
                inputStream.removeBytes(it.first, it.last - it.first + 1)
            }
            return CommandResponse<Path>(
                inputStream,
                fileName
            ).withResponseData(path)
        }
    }

    override fun handleFirstResponse(response: Message, event: MessageReceivedEvent, responseData: Path?) {
        responseData?.deleteIfExists()
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
        // First 6 bytes should be the GIF header
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
        // Bit 1
        val globalColorTableFlag = packed and 128 != 0
        // Bits 6-8
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
        // Read GIF file content blocks
        var continueReading = true
        while (continueReading) {
            // Code
            when (inputStream.read()) {
                // Extension introducer
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
        // Extension label
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
        // Bit 1
        val localColorTableFlag = packed and 128 != 0
        // Bits 6-8
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
        // Loop until the block terminator, 0x00, or the end of the stream, -1, is reached
        do {
            blockSize = skipBlock(inputStream)
        } while (blockSize > 0)
    }

    private fun skipBlock(inputStream: InputStream): Int {
        // The first byte is the block size
        val blockSize = inputStream.read()
        inputStream.skipNBytes(blockSize.toLong())
        return blockSize
    }

    private fun createApplicationExtension(loopCount: Int): List<Int> {
        val applicationExtension = mutableListOf<Int>()
        applicationExtension.add(0x21)                              // Extension code
        applicationExtension.add(0xFF)                              // Application extension label
        applicationExtension.add(0x0B)                              // Length of Application block, 11 bytes
        "NETSCAPE2.0".forEach { applicationExtension.add(it.code) } // Application identifier
        applicationExtension.add(0x03)                              // Length of data sub-block, 3 bytes
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
