package io.github.shaksternano.borgar.core.task

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.KClass
import kotlin.test.Test

class TaskTest {

    @Test
    fun canExecuteTask() = runTest {
        val task = AdditionTask(5)
        val result = task.execute(10)
        assert(result == 15)
    }

    @Test
    fun canChainTasks() = runTest {
        val task1 = AdditionTask(5)
        val task2 = RepeatStringTask("abc")
        assert(task1.isCompatible(task2))
        val chained = task1 then task2
        val result = chained.execute(10)
        assert(result == "abc".repeat(15))
    }

    @Test
    fun cannotChainIncompatibleTasks() {
        val task1 = RepeatStringTask("abc")
        @Suppress("UNCHECKED_CAST")
        val task2 = AdditionTask(5) as Task<String, *>
        assert(!task1.isCompatible(task2))
        assertThrows<IllegalArgumentException> {
            task1 then task2
        }
    }
}

private class AdditionTask(
    private val toAdd: Int,
) : Task<Int, Int> {
    override val inputType: KClass<Int> = Int::class
    override val outputType: KClass<Int> = Int::class

    override suspend fun execute(input: Int): Int {
        return input + toAdd
    }
}

private class RepeatStringTask(
    private val toRepeat: String,
) : Task<Int, String> {
    override val inputType: KClass<Int> = Int::class
    override val outputType: KClass<String> = String::class

    override suspend fun execute(input: Int): String {
        return toRepeat.repeat(input)
    }
}
