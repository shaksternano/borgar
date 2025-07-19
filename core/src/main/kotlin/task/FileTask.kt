package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.collect.parallelForEach
import io.github.shaksternano.borgar.core.collect.parallelMap
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.io.deleteSilently
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue

interface FileTask : SuspendCloseable {

    val requireInput: Boolean
    val suppliedInput: DataSource?
        get() = null

    suspend fun run(input: List<DataSource>): List<DataSource>

    infix fun then(after: FileTask): FileTask {
        if (!after.requireInput) {
            throw UnsupportedOperationException("The task after this one must require input")
        }
        return ChainedFileTask(this, after)
    }

    override suspend fun close() = Unit
}

private class ChainedFileTask(
    private val first: FileTask,
    private val second: FileTask,
) : FileTask {

    override val requireInput: Boolean = first.requireInput

    override suspend fun run(input: List<DataSource>): List<DataSource> {
        val firstOutput = first.run(input)
        return second.run(firstOutput)
    }

    override suspend fun close() = closeAll(
        first,
        second,
    )

    override fun toString(): String {
        return "ChainedFileTask(first=$first, second=$second, requireInput=$requireInput)"
    }
}

suspend fun FileTask.run(): List<DataSource> =
    if (requireInput) {
        val suppliedInput = suppliedInput
        if (suppliedInput == null) {
            throw UnsupportedOperationException("This task requires input")
        } else {
            run(listOf(suppliedInput))
        }
    } else {
        run(listOfNotNull(suppliedInput))
    }

abstract class BaseFileTask : FileTask {

    private val toDelete: MutableCollection<Path> = ConcurrentLinkedQueue()

    protected fun markToDelete(path: Path) {
        toDelete.add(path)
    }

    override suspend fun close() {
        toDelete.parallelForEach(Path::deleteSilently)
        toDelete.clear()
    }
}

abstract class MappedFileTask : BaseFileTask() {

    override val requireInput: Boolean = true

    final override suspend fun run(input: List<DataSource>): List<DataSource> = input.parallelMap {
        process(it).also { output ->
            output.path?.let(this::markToDelete)
        }
    }

    protected abstract suspend fun process(input: DataSource): DataSource
}
