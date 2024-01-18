package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.core.util.formatted
import io.github.shaksternano.borgar.core.util.kClass

data class CommandArgumentInfo<T>(
    val key: String,
    val aliases: Set<String> = emptySet(),
    val description: String = "",
    val type: CommandArgumentType<T>,
    val required: Boolean = true,
    val defaultValue: T? = null,
    val validator: Validator<T> = allowAllValidator(),
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false
        other as CommandArgumentInfo<*>
        return key == other.key
    }

    override fun hashCode(): Int = key.hashCode()
}

val CommandArgumentInfo<*>.keyWithPrefix: String
    get() = ARGUMENT_PREFIX + key

interface Validator<T> {
    fun validate(value: T): Boolean

    fun errorMessage(value: T, key: String): String
}

private fun <T> allowAllValidator(): Validator<T> {
    @Suppress("UNCHECKED_CAST")
    return AllowAllValidator as Validator<T>
}

private object AllowAllValidator : Validator<Any?> {
    override fun validate(value: Any?): Boolean = true

    override fun errorMessage(value: Any?, key: String): String = ""
}

class RangeValidator<T : Comparable<T>>(
    val range: ClosedRange<T>,
) : Validator<T> {

    override fun validate(value: T): Boolean = value in range

    override fun errorMessage(value: T, key: String): String =
        "The value for **$key** must be between ${range.start.formatted} and ${range.endInclusive.formatted}."
}


val ZERO_TO_ONE_VALIDATOR: Validator<Double> = RangeValidator(0.0..1.0)

open class MinValueValidator<T : Comparable<T>>(
    val minValue: T,
) : Validator<T> {

    override fun validate(value: T): Boolean = value >= minValue

    override fun errorMessage(value: T, key: String): String =
        "The value for **$key** must be greater than or equal to ${minValue.formatted}."
}

object PositiveLongValidator : MinValueValidator<Long>(
    minValue = 1,
) {

    override fun validate(value: Long): Boolean = value > 0

    override fun errorMessage(value: Long, key: String): String =
        "The value for **$key** must be positive."
}
