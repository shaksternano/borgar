package io.github.shaksternano.borgar.core.exception

class ErrorResponseException(
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
