package io.github.shaksternano.borgar.core.exception

class ErrorResponseException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause)
