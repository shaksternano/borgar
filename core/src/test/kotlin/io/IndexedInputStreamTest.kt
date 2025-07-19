@file:Suppress("PackageDirectoryMismatch")

package io.github.shaksternano.borgar.core.io

import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class IndexedInputStreamTest {

    @Test
    fun testReadUpdatesIndex() {
        val inputData = "Hello, World!".encodeToByteArray()
        val inputStream = ByteArrayInputStream(inputData).indexed()

        assertEquals(0, inputStream.nextIndex)

        // Read first byte
        val byte = inputStream.read()
        assertEquals('H'.code, byte)
        assertEquals(1, inputStream.nextIndex)

        // Read the next 5 bytes
        val buffer = ByteArray(5)
        inputStream.read(buffer)
        assertContentEquals("ello,".encodeToByteArray(), buffer)
        assertEquals(6, inputStream.nextIndex)
    }

    @Test
    fun testSkipUpdatesIndex() {
        val inputData = "Hello, World!".encodeToByteArray()
        val inputStream = ByteArrayInputStream(inputData).indexed()

        assertEquals(0, inputStream.nextIndex)

        // Skip 7 bytes
        val skipped = inputStream.skip(7)
        assertEquals(7, skipped)
        assertEquals(7, inputStream.nextIndex)

        // Read next byte
        val byte = inputStream.read()
        assertEquals('W'.code, byte)
        assertEquals(8, inputStream.nextIndex)
    }

    @Test
    fun testMarkAndReset() {
        val inputData = "Hello, World!".encodeToByteArray()
        val inputStream = ByteArrayInputStream(inputData).indexed()

        // Read 6 bytes
        inputStream.skip(6)
        assertEquals(6, inputStream.nextIndex)

        // Mark the current position
        inputStream.mark(100)

        // Read 5 more bytes
        inputStream.skip(5)
        assertEquals(11, inputStream.nextIndex)

        // Reset to a marked position
        inputStream.reset()
        assertEquals(6, inputStream.nextIndex)

        // Verify we're at the right position
        val byte = inputStream.read()
        assertEquals(' '.code, byte)  // The character at position 6 is a space
        assertEquals(7, inputStream.nextIndex)
    }
}
