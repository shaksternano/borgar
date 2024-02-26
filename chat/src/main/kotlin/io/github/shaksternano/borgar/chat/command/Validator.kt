package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.core.util.formatted

interface Validator<T> {
    fun validate(value: T): Boolean

    fun errorMessage(value: T, key: String): String
}

fun <T> allowAllValidator(): Validator<T> {
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

    companion object {
        val ZERO_TO_ONE: Validator<Double> = RangeValidator(0.0..1.0)
    }

    override fun validate(value: T): Boolean = value in range

    override fun errorMessage(value: T, key: String): String =
        "The argument **$key** must be between ${range.start.formatted} and ${range.endInclusive.formatted}."
}

open class MinValueValidator<T : Comparable<T>>(
    val minValue: T,
) : Validator<T> {

    override fun validate(value: T): Boolean = value >= minValue

    override fun errorMessage(value: T, key: String): String =
        "The argument **$key** must be greater than or equal to ${minValue.formatted}."
}

object PositiveIntValidator : MinValueValidator<Int>(
    minValue = 1,
) {

    override fun validate(value: Int): Boolean = value > 0

    override fun errorMessage(value: Int, key: String): String =
        "The argument **$key** must be positive."
}

object PositiveDoubleValidator : MinValueValidator<Double>(
    minValue = 0.0,
) {

    override fun validate(value: Double): Boolean = value > 0.0

    override fun errorMessage(value: Double, key: String): String =
        "The argument **$key** must be positive."
}

object GreaterThanOneValidator : MinValueValidator<Double>(
    minValue = 1.0,
) {

    override fun validate(value: Double): Boolean = value > 1.0

    override fun errorMessage(value: Double, key: String): String =
        "The argument **$key** must be greater than 1."
}
