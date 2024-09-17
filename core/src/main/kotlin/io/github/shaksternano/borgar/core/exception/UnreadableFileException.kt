package io.github.shaksternano.borgar.core.exception

import kotlinx.io.IOException

class UnreadableFileException(
    override val cause: Throwable,
) : IOException(cause)
