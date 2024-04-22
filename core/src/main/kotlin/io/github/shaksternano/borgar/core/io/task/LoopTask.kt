package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.*
import java.io.EOFException
import java.io.InputStream
import kotlin.math.pow

class LoopTask(
    loopCount: Int,
) : MappedFileTask() {

    private val loopCount: Int = when (loopCount) { // Swap 0 and -1
        // Remove looping
        0 -> -1
        // Loop forever
        -1 -> 0
        else -> loopCount
    }

    override suspend fun process(input: DataSource): DataSource {
        val tempFile = input.path == null
        val dataSource = input.getOrWriteFile()
        if (tempFile) {
            markToDelete(dataSource.path)
        }
        val gifInfo = try {
            locateGifComponents(dataSource.newStream())
        } catch (e: IllegalArgumentException) {
            throw ErrorResponseException("File is not a GIF!")
        }
        val applicationExtension =
            // Do not add the application extension if no looping is wanted
            if (loopCount < 0) listOf()
            else createApplicationExtension(loopCount)
        return DataSource.fromStreamSupplier(input.filename) {
            val result = dataSource.newStream()
                .buffered()
                .modifiable()
            // Insert the new application extension with the specified loop count after the global color table
            result.insertBytes(gifInfo.globalColorTableEnd, applicationExtension)
            // Remove all existing application extensions
            gifInfo.applicationExtensions.forEach {
                result.removeBytes(it.first, it.last - it.first + 1)
            }
            result
        }
    }

    /*
    GIF specification:
    https://giflib.sourceforge.net/whatsinagif/bits_and_bytes.html
     */
    private suspend fun locateGifComponents(inputStream: InputStream): GifInfo =
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

    private suspend fun readHeader(inputStream: InputStream) {
        // First 6 bytes should be the GIF header
        val bytes = inputStream.readNBytesSuspend(6)
        // Should either be GIF87a or GIF89a
        val id = bytes.decodeToString()
        if (!id.startsWith("GIF")) {
            throw IllegalArgumentException("File doesn't start with GIF header")
        }
    }

    private suspend fun readLogicalScreenDescriptor(inputStream: InputStream): Long {
        /*
        Bytes:
        1-2 : canvas width
        3-4 : canvas height
         */
        inputStream.skipNBytesSuspend(4)

        /*
        Bits:
        1   : global color table flag
        2-4 : color resolution
        5   : global color table sort flag
        6-8 : global color table size
         */
        val packed = inputStream.readSuspend()
        // Bit 1
        val globalColorTableFlag = packed and 128 != 0
        // Bits 6-8
        val globalColorTableSize = packed and 7

        /*
        Bytes:
        1 : background color index
        2 : pixel aspect ratio
         */
        inputStream.skipNBytesSuspend(2)

        return if (globalColorTableFlag) {
            calculateColorTableBytes(globalColorTableSize)
        } else {
            0
        }
    }

    private fun calculateColorTableBytes(colorTableSize: Int): Long =
        /*
        Color table bytes = 2^(n + 1) * 3:
            2^(n + 1) colors, where n is the color table size
            3 bytes per color
         */
        2.0.pow(colorTableSize + 1).toLong() * 3

    private suspend fun readColorTable(inputStream: InputStream, size: Long) =
        inputStream.skipNBytesSuspend(size)

    private suspend fun readContent(inputStream: IndexedInputStream): List<LongRange> {
        /*
        Some GIFs have more than one application extension, which is invalid.
        These will all be removed.
         */
        val applicationExtensions = mutableListOf<LongRange>()
        // Read GIF file content blocks
        var continueReading = true
        while (continueReading) {
            // Code
            when (inputStream.readSuspend()) {
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

    private suspend fun readExtension(inputStream: IndexedInputStream): LongRange? {
        // Extension label
        when (inputStream.readSuspend()) {
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

    private suspend fun readImage(inputStream: InputStream) {
        val localColorTableBytes = readImageDescriptor(inputStream)
        readColorTable(inputStream, localColorTableBytes)
        readImageData(inputStream)
    }

    private suspend fun readImageDescriptor(inputStream: InputStream): Long {
        /*
        Bytes:
        1-2 : image left position
        3-4 : image top position
        5-6 : image width
        7-8 : image height
         */
        inputStream.skipNBytesSuspend(8)
        /*
        Bits:
        1   : local color table flag
        2   : interlace flag
        3   : sort flag
        4-5 : reserved for future use
        6-8 : local color table size
         */
        val packed = inputStream.readSuspend()
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

    private suspend fun readImageData(inputStream: InputStream) {
        // LZW minimum code size
        inputStream.skipNBytesSuspend(1)
        skipBlocks(inputStream)
    }

    private suspend fun skipBlocks(inputStream: InputStream) {
        var blockSize: Int
        // Loop until the block terminator, 0x00, or the end of the stream, -1, is reached
        do {
            blockSize = skipBlock(inputStream)
        } while (blockSize > 0)
    }

    private suspend fun skipBlock(inputStream: InputStream): Int {
        // The first byte is the block size
        val blockSize = inputStream.readSuspend()
        inputStream.skipNBytesSuspend(blockSize.toLong())
        return blockSize
    }

    private fun createApplicationExtension(loopCount: Int): List<Int> = buildList {
        add(0x21)                              // Extension code
        add(0xFF)                              // Application extension label
        add(0x0B)                              // Length of Application block, 11 bytes
        "NETSCAPE2.0".forEach { add(it.code) } // Application identifier
        add(0x03)                              // Length of data sub-block, 3 bytes
        add(0x01)                              // Constant
        add(loopCount and 0xFF)                // Loop count in two byte little-endian format.
        add((loopCount shr 8) and 0xFF)
        add(0x00)                              // Data Sub-Block Terminator
    }

    private data class GifInfo(
        val globalColorTableEnd: Long,
        val applicationExtensions: List<LongRange>,
    )
}
