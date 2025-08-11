package com.shakster.borgar.core.exception

import io.ktor.http.*
import kotlinx.io.IOException

class ErrorResponseException(
    override val message: String = "",
    override val cause: Throwable? = null,
) : Exception(message, cause)

class UnreadableFileException(
    override val cause: Throwable? = null,
) : IOException(cause)

class UnsupportedMediaTypeException() : IOException()

class HttpException(
    override val message: String,
    val status: HttpStatusCode,
) : IOException(message)

class FileTooLargeException(
    override val cause: Throwable? = null,
) : Exception(cause)
