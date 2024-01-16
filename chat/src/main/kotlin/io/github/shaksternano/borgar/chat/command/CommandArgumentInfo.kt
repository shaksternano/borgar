package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.core.util.formatted
import io.github.shaksternano.borgar.core.util.kClass

data class CommandArgumentInfo<T>(
    val key: String,
    val description: String = "",
    val type: CommandArgumentType<T>,
    val required: Boolean = true,
    val defaultValue: T? = null,
    val validator: Validator<T> = AllowAllValidator(),
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

private class AllowAllValidator<T> : Validator<T> {
    override fun validate(value: T): Boolean = true

    override fun errorMessage(value: T, key: String): String = ""
}

class LongRangeValidator(
    private val range: LongRange,
) : Validator<Long> {

    override fun validate(value: Long): Boolean = value in range

    override fun errorMessage(value: Long, key: String): String =
        "Value for `$key` must be in range ${range.first}..${range.last}."
}

class DoubleRangeValidator(
    private val range: ClosedFloatingPointRange<Double>,
) : Validator<Double> {

    override fun validate(value: Double): Boolean = value in range

    override fun errorMessage(value: Double, key: String): String =
        "The value for `$key` must be in range the ${range.start.formatted} to ${range.endInclusive.formatted}."
}

val ZERO_TO_ONE_VALIDATOR: Validator<Double> = DoubleRangeValidator(0.0..1.0)
