package io.github.shaksternano.borgar.core.io

import io.ktor.utils.io.errors.*
import java.io.InputStream

class ModifiableInputStream(
    inputStream: InputStream
) : IndexedInputStream(inputStream) {

    private val toInsert: MutableMap<Long, List<Int>> = mutableMapOf()
    private val inserting: MutableList<Int> = mutableListOf()
    private val toRemove: MutableMap<Long, Long> = mutableMapOf()

    fun insertBytes(index: Long, bytes: List<Int>) {
        toInsert[index] = bytes
    }

    fun removeBytes(index: Long, length: Long) {
        toRemove[index] = length
    }

    override fun read(): Int {
        fillInserting()
        val inserted = inserting.removeFirstOrNull()
        if (inserted != null) {
            return inserted
        }
        val removedLength = toRemove.remove(nextIndex)
        if (removedLength != null) {
            try {
                skipNBytes(removedLength)
            } catch (e: EOFException) {
                return -1
            }
        }
        return super.read()
    }

    private fun fillInserting() {
        if (inserting.isEmpty()) {
            val newInserting = toInsert.remove(nextIndex)
            if (newInserting != null) {
                inserting.addAll(newInserting)
            }
        }
    }
}

fun InputStream.modifiable(): ModifiableInputStream = ModifiableInputStream(this)
