package com.shakster.borgar.core.logging

import org.slf4j.event.Level

fun interface LoggerHook {

    fun onLog(
        level: Level,
        message: String?,
        t: Throwable?,
        vararg arguments: Any?,
    )
}
