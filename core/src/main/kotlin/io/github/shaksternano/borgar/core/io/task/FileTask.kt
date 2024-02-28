package io.github.shaksternano.borgar.core.io.task

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

            override suspend fun close() = closeAll(
                this@FileTask,
                after,
            )
        }
    }

    override suspend fun close() = Unit
}

abstract class BaseFileTask(
    final override val requireInput: Boolean,
) : FileTask {

    private val toDelete: MutableCollection<Path> = ConcurrentLinkedQueue()

    protected fun markToDelete(path: Path) {
        toDelete.add(path)
    }

    override suspend fun close() {
        toDelete.parallelForEach(Path::deleteSilently)
        toDelete.clear()
    }
}

abstract class MappedFileTask : BaseFileTask(
    requireInput = true,
) {

    final override suspend fun run(input: List<DataSource>): List<DataSource> = input.parallelMap {
        process(it).also { output ->
            output.path?.let(this::markToDelete)
        }
    }

    abstract suspend fun process(input: DataSource): DataSource
}
