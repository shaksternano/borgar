package io.github.shaksternano.borgar.core.graphics

import io.github.shaksternano.borgar.core.io.forEachResource
import io.github.shaksternano.borgar.core.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.InputStream

const val DEFAULT_FONT_NAME = Font.DIALOG

suspend fun registerFonts() = forEachResource(
    "font"
) { resourcePath: String, inputStream: InputStream ->
    runCatching {
        val font = withContext(Dispatchers.IO) {
            Font.createFont(Font.TRUETYPE_FONT, inputStream)
        }
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font)
    }.onFailure {
        logger.error("Error loading font $resourcePath", it)
    }
}

fun fontExists(fontName: String): Boolean =
    GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts.any {
        it.name == fontName
    }
