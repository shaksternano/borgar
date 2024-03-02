package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.io.DataSource
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class FileTaskTest {

    @Test
    fun canChainTasks() = runTest {
        val task1 = StringConcatTask("b")
        val task2 = StringConcatTask("c")
        val inputBytes = "a".toByteArray()
        val input = DataSource.fromBytes("input", inputBytes)
        val chained = task1 then task2
        val result = chained.run(listOf(input))
        assertEquals(1, result.size)
        val output = result.first()
        val outputBytes = output.newStream().readAllBytes()
        val outputString = String(outputBytes)
        assertEquals("abc", outputString)
    }

    @Test
    fun taskMustRequireInput() = runTest {
        val task1 = StringConcatTask("b")
        val task2 = NoInputTask()
        assertThrows<UnsupportedOperationException> {
            task1 then task2
        }
    }
}

private class StringConcatTask(
    private val toConcat: String,
) : FileTask {
    override val requireInput: Boolean = true

    override suspend fun run(input: List<DataSource>): List<DataSource> {
        return input.map {
            val string = String(it.newStream().readAllBytes())
            val output = string + toConcat
            DataSource.fromBytes("string", output.toByteArray())
        }
    }
}

private class NoInputTask : FileTask {

    override val requireInput: Boolean = false

    override suspend fun run(input: List<DataSource>): List<DataSource> = throw IllegalStateException()
}
