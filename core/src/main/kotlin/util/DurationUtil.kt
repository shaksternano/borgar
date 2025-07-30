package com.shakster.borgar.core.util

import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

operator fun Duration.rem(scale: Long): Duration =
    (inWholeNanoseconds % scale).nanoseconds

fun Duration.circular(total: Duration): Duration =
    this % max(total.inWholeNanoseconds, 1)
