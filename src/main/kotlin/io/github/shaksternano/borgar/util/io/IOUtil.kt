package io.github.shaksternano.borgar.util.io

import java.nio.file.Files
import java.nio.file.Path

fun createTemporaryFile(nameWithoutExtension: String, extension: String): Path {
    val extensionWithDot = if (extension.isBlank()) "" else ".$extension"
    val path = Files.createTempFile(nameWithoutExtension, extensionWithDot)
    path.toFile().deleteOnExit()
    return path
}
