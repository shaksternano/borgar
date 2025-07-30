package com.shakster.borgar.core.io

import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*

class HttpByteReadChannel(
    private val url: String,
) : DelegatedByteReadChannel() {

    private val httpClient: HttpClient = configuredHttpClient(json = false)

    override suspend fun createChannel(): ByteReadChannel {
        val response = httpClient.get(url)
        return response.bodyAsChannel()
    }

    override fun cancel(cause: Throwable?) {
        httpClient.close()
        return super.cancel(cause)
    }
}
