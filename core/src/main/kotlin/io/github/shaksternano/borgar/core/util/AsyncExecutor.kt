package io.github.shaksternano.borgar.core.util

import com.shakster.gifkt.SuspendClosable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlin.coroutines.EmptyCoroutineContext

class AsyncExecutor<in T, out R>(
    maxConcurrency: Int,
    private val scope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    private val task: suspend (T) -> R,
    private val onOutput: suspend (Result<R>) -> Unit,
) : SuspendClosable {

    private val semaphore: Semaphore = Semaphore(maxConcurrency)
    private val outputChannel: Channel<Deferred<Result<R>>> = Channel(maxConcurrency)

    private val outputJob: Job = scope.launch {
        for (output in outputChannel) {
            onOutput(output.await())
        }
    }

    suspend fun submit(input: T) {
        semaphore.acquire()
        val deferred = scope.async {
            try {
                Result.success(task(input))
            } catch (t: Throwable) {
                Result.failure(t)
            } finally {
                semaphore.release()
            }
        }
        outputChannel.send(deferred)
    }

    override suspend fun close() {
        outputChannel.close()
        outputJob.join()
    }
}
