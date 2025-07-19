package io.github.shaksternano.borgar.core.graphics.drawable

import io.github.shaksternano.borgar.core.graphics.TextAlignment
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.util.*
import java.awt.Graphics2D
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

class ParagraphDrawable(
    text: String,
    nonTextParts: Map<String, Drawable>,
    private val alignment: TextAlignment,
    maxWidth: Int,
    textDrawableFactory: (String) -> Drawable = ::SimpleTextDrawable,
) : Drawable {

    companion object {
        private val NEWLINE: Drawable = SimpleTextDrawable("\n")
    }

    private val maxWidth: Int = max(0, maxWidth)
    private val parts: List<Drawable> = run {
        val wordMatches = NON_WHITESPACE_REGEX.findAll(text)
        val whitespaceMatches = HORIZONTAL_WHITESPACE_REGEX.findAll(text)
        val newlineMatches = NEWLINE_REGEX.findAll(text)

        val orderedMatches = (wordMatches + whitespaceMatches + newlineMatches)
            .sortedBy { it.range.first }

        val sortedNonTextParts = nonTextParts.entries
            .sortedBy { it.key.length }
            .reversed()
            .map { it.key to it.value }
        orderedMatches.map {
            val match = it.value
            if (NON_WHITESPACE_REGEX.matches(match)) {
                val parts = getWordParts(match, sortedNonTextParts, textDrawableFactory)
                if (parts.size == 1) {
                    parts.first()
                } else {
                    HorizontalDrawable(parts)
                }
            } else if (NEWLINE_REGEX.matches(match)) {
                NEWLINE
            } else {
                SimpleTextDrawable(match)
            }
        }.toList()
    }

    private fun getWordParts(
        word: String,
        nonTextParts: Collection<Pair<String, Drawable>>,
        textDrawableFactory: (String) -> Drawable,
    ): List<Drawable> {
        if (nonTextParts.isEmpty()) {
            return listOf(textDrawableFactory(word))
        }
        nonTextParts.forEach {
            val key = it.first
            val index = word.indexOf(key)
            if (index >= 0) {
                val before = word.substring(0, index)
                val after = word.substring(index + key.length)
                if (before.isBlank() && after.isBlank()) {
                    return listOf(it.second)
                }
                val parts = mutableListOf<Drawable>()
                if (before.isNotBlank()) {
                    parts.addAll(getWordParts(before, nonTextParts, textDrawableFactory))
                }
                parts.add(it.second)
                if (after.isNotBlank()) {
                    parts.addAll(getWordParts(after, nonTextParts, textDrawableFactory))
                }
                return parts
            }
        }
        return listOf(textDrawableFactory(word))
    }

    private suspend fun drawAndGetSize(
        graphics: Graphics2D,
        x: Int,
        y: Int,
        timestamp: Duration,
        draw: Boolean,
    ): Pair<Int, Int> {
        val metrics = graphics.fontMetrics
        val lineHeight = metrics.ascent + metrics.descent
        val lineSpace = metrics.leading
        val lineHeightAndSpace = lineHeight + lineSpace
        var lineWidth = 0
        var lineY = y
        var maxLineWidth = 0

        val currentLine = mutableListOf<Drawable>()
        for (part in parts) {
            if (part == NEWLINE) {
                if (draw) {
                    drawLine(graphics, currentLine, x, lineWidth, lineY, timestamp)
                }
                currentLine.clear()
                lineWidth = 0
                lineY += lineHeightAndSpace
                continue
            }

            val resizedPart = part.resizeToHeight(lineHeight) ?: part
            val partWidth = resizedPart.getWidth(graphics)
            val newLineWidth = lineWidth + partWidth
            if (newLineWidth <= maxWidth || lineWidth == 0) {
                currentLine.add(resizedPart)
                lineWidth = newLineWidth
            } else {
                val allBlank = currentLine.all {
                    it is TextDrawable && it.text.isBlank()
                }
                if (draw && !allBlank) {
                    drawLine(graphics, currentLine, x, lineWidth, lineY, timestamp)
                }
                currentLine.clear()
                currentLine.add(resizedPart)
                lineWidth = min(partWidth, maxWidth)
                if (!allBlank) {
                    lineY += lineHeightAndSpace
                }
            }
            maxLineWidth = max(maxLineWidth, lineWidth)
        }

        var lineX = calculateTextXPosition(alignment, x, lineWidth, maxWidth)
        lineWidth = 0
        currentLine.forEach {
            if (draw) {
                it.draw(graphics, lineX, lineY, timestamp)
            }
            val width = it.getWidth(graphics)
            lineX += width
            lineWidth += width
        }
        maxLineWidth = max(maxLineWidth, lineWidth)
        if (currentLine.isNotEmpty()) {
            lineY += lineHeight
        }
        return maxLineWidth to lineY
    }

    override suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration) {
        drawAndGetSize(graphics, x, y, timestamp, true)
    }

    private suspend fun drawLine(
        graphics: Graphics2D,
        line: Collection<Drawable>,
        x: Int,
        lineWidth: Int,
        lineY: Int,
        timestamp: Duration,
    ) {
        if (line.isEmpty()) return
        var lineX = calculateTextXPosition(alignment, x, lineWidth, maxWidth)
        line.forEach {
            it.draw(graphics, lineX, lineY, timestamp)
            lineX += it.getWidth(graphics)
        }
    }

    private fun calculateTextXPosition(alignment: TextAlignment, x: Int, lineWidth: Int, maxWidth: Int): Int =
        when (alignment) {
            TextAlignment.CENTRE -> x + (maxWidth - lineWidth) / 2
            TextAlignment.RIGHT -> x + maxWidth - lineWidth
            else -> x
        }

    override suspend fun getWidth(graphics: Graphics2D): Int =
        drawAndGetSize(graphics, 0, 0, Duration.ZERO, false).first

    override suspend fun getHeight(graphics: Graphics2D): Int =
        drawAndGetSize(graphics, 0, 0, Duration.ZERO, false).second

    override fun resizeToHeight(height: Int): Drawable? = null

    override suspend fun close() =
        closeAll(parts)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false

        other as ParagraphDrawable

        if (alignment != other.alignment) return false
        if (parts != other.parts) return false
        if (maxWidth != other.maxWidth) return false

        return true
    }

    override fun hashCode(): Int = hash(
        alignment,
        parts,
        maxWidth,
    )

    override fun toString(): String {
        return "ParagraphCompositeDrawable(alignment=$alignment" +
            ", parts=$parts" +
            ", maxWidth=$maxWidth)"
    }
}
