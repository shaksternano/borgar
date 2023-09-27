package io.github.shaksternano.borgar.core.collect

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

abstract class AsyncIterator<T>(
    coroutineScope: CoroutineScope
) : Iterator<Deferred<T>>, CoroutineScope by coroutineScope

abstract class AsyncCloseableIterator<T>(
    coroutineScope: CoroutineScope
) : AsyncIterator<T>(coroutineScope), CloseableIterator<Deferred<T>>
