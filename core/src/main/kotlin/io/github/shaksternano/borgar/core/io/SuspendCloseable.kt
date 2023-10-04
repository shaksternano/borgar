package io.github.shaksternano.borgar.core.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun interface SuspendCloseable {
    suspend fun close()

    companion object {
        fun fromBlocking(closeable: AutoCloseable): SuspendCloseable = SuspendCloseable {
            withContext(Dispatchers.IO) {
                closeable.close()
            }
        }

        fun fromBlocking(block: () -> Unit): SuspendCloseable = SuspendCloseable {
            withContext(Dispatchers.IO) {
                block()
            }
        }
    }
}

fun SuspendCloseable(closeable: AutoCloseable): SuspendCloseable =
    SuspendCloseable(closeable::close)

/**
 * Executes the given [block] function on this resource and then closes it down correctly whether an exception
 * is thrown or not.
 *
 * @param block a function to process this [SuspendCloseable] resource.
 * @return the result of [block] function invoked on this resource.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <T : SuspendCloseable?, R> T.use(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        closeFinally(exception)
    }
}

/**
 * Closes this [SuspendCloseable], suppressing possible exception or error thrown by [SuspendCloseable.close] function when
 * it's being closed due to some other [cause] exception occurred.
 *
 * The suppressed exception is added to the list of suppressed exceptions of [cause] exception, when it's supported.
 */
@PublishedApi
internal suspend fun SuspendCloseable?.closeFinally(cause: Throwable?) = when {
    this == null -> {}
    cause == null -> close()
    else ->
        try {
            close()
        } catch (closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
}

suspend fun closeAll(vararg closeables: SuspendCloseable?) =
    closeAll(closeables.asIterable())

suspend fun closeAll(closeables: Iterable<SuspendCloseable?>) {
    var throwable: Throwable? = null
    closeables.forEach {
        try {
            it?.close()
        } catch (e: Throwable) {
            throwable?.let(e::addSuppressed) ?: run {
                throwable = e
            }
        }
    }
    throwable?.let { throw it }
}

suspend inline fun <A : SuspendCloseable?, B : SuspendCloseable?, R> useAll(
    closeable1: A,
    closeable2: B,
    block: (A, B) -> R,
): R = useAllIgnored(closeable1, closeable2) {
    block(closeable1, closeable2)
}

suspend inline fun <A : SuspendCloseable?, B : SuspendCloseable?, C : SuspendCloseable?, R> useAll(
    closeable1: A,
    closeable2: B,
    closeable3: C,
    block: (A, B, C) -> R,
): R = useAllIgnored(closeable1, closeable2, closeable3) {
    block(closeable1, closeable2, closeable3)
}

suspend inline fun <R> useAllIgnored(vararg closeables: SuspendCloseable?, block: () -> R): R {
    val closeable = SuspendCloseable {
        closeAll(*closeables)
    }
    return closeable.use {
        block()
    }
}
