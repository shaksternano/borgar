package io.github.shaksternano.borgar.core.exception

import java.io.IOException

class UnreadableFileException(
    override val cause: Throwable,
) : IOException(cause)
