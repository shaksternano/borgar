package io.github.shaksternano.borgar.core.task

import kotlin.reflect.KClass

interface Task<T : Any, R : Any> {

    val inputType: KClass<T>
    val outputType: KClass<R>

    suspend fun execute(input: T): R

    fun isCompatible(after: Task<*, *>): Boolean {
        return after.inputType == outputType
    }

    infix fun <U : Any> then(after: Task<R, U>): Task<T, U> {
        if (!isCompatible(after)) {
            throw IllegalArgumentException("Cannot chain incompatible tasks")
        }
        return object : Task<T, U> {
            override val inputType: KClass<T> = this@Task.inputType
            override val outputType: KClass<U> = after.outputType

            override suspend fun execute(input: T): U {
                val firstOutput = this@Task.execute(input)
                return after.execute(firstOutput)
            }
        }
    }
}
