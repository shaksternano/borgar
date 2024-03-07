package io.github.shaksternano.borgar.core.util

import kotlin.math.pow

infix fun Int.pow(exponent: Int): Long =
    toDouble().pow(exponent).toLong()
