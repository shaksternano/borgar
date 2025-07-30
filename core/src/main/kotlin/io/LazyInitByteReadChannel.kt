package com.shakster.borgar.core.io

import io.ktor.utils.io.*

class LazyInitByteReadChannel(
    private val provider: suspend () -> ByteReadChannel,
) : DelegatedByteReadChannel() {

    override suspend fun createChannel(): ByteReadChannel = provider()
}
