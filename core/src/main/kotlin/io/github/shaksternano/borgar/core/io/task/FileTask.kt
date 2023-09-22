package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

interface FileTask {

    val requireInput: Boolean

    suspend fun run(input: List<DataSource>): List<DataSource>

    infix fun then(after: FileTask): FileTask {
        if (!after.requireInput) {
            throw UnsupportedOperationException("The task after this one must require input")
        }
        return object : FileTask {
            override val requireInput: Boolean = this@FileTask.requireInput

            override suspend fun run(input: List<DataSource>): List<DataSource> {
                val firstOutput = this@FileTask.run(input)
                return after.run(firstOutput)
            }

            override suspend fun cleanup() {
                this@FileTask.cleanup()
                after.cleanup()
            }
        }
    }

    suspend fun cleanup() = Unit
}

abstract class BaseFileTask(
    final override val requireInput: Boolean,
) : FileTask {

    private val outputs: MutableList<Path> = mutableListOf()

    protected fun addOutput(path: Path) = outputs.add(path)

    override suspend fun cleanup() {
        withContext(Dispatchers.IO) {
            outputs.forEach {
                it.deleteIfExists()
            }
        }
        outputs.clear()
    }
}

abstract class MappedFileTask(
    requireInput: Boolean,
) : BaseFileTask(requireInput) {

    final override suspend fun run(input: List<DataSource>): List<DataSource> = input.map {
        val output = process(it)
        output.path?.let(this::addOutput)
        output
    }

    abstract suspend fun process(input: DataSource): DataSource
}
