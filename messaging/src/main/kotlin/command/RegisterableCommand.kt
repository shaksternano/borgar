package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.util.Named

interface RegisterableCommand : Named {

    val register: Boolean
        get() = true
}
