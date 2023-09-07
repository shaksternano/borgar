package io.github.shaksternano.borgar.core.io

import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.appendBytes
import kotlin.io.path.createTempFile

suspend fun createTemporaryFile(nameWithoutExtension: String, extension: String): Path {
    val extensionWithDot = if (extension.isBlank()) "" else ".$extension"
    val path = withContext(Dispatchers.IO) {
        createTempFile(nameWithoutExtension, extensionWithDot)
    }
    path.toFile().deleteOnExit()
    return path
}

suspend fun download(response: HttpResponse, path: Path) {
    val channel = response.bodyAsChannel()
    while (!channel.isClosedForRead) {
        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
        while (!packet.isEmpty) {
            val bytes = packet.readBytes()
            withContext(Dispatchers.IO) {
                path.appendBytes(bytes)
            }
        }
    }
}
