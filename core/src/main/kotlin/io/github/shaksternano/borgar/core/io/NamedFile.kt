package io.github.shaksternano.borgar.core.io

import com.google.common.io.Files
import java.io.File
import java.nio.file.Path

data class NamedFile(val path: Path, val name: String) {

    val file: File
        get() = path.toFile()

    constructor(file: File) : this(file.toPath(), file.getName())

    constructor(file: File, name: String) : this(file.toPath(), name)

    constructor(file: File, nameWithoutExtension: String, extension: String) : this(
        file.toPath(), filename(nameWithoutExtension, extension)
    )

    constructor(path: Path, nameWithoutExtension: String, extension: String) : this(
        path, filename(nameWithoutExtension, extension)
    )

    fun nameWithoutExtension(): String {
        return Files.getNameWithoutExtension(name)
    }

    fun extension(): String {
        return FileUtil.getFileExtension(name)
    }
}
