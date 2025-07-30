package com.shakster.borgar.core.util

fun <T> ((T) -> T)?.then(after: (T) -> T): (T) -> T =
    { after(this?.invoke(it) ?: it) }
