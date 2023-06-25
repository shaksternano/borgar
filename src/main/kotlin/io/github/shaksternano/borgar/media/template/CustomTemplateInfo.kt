package io.github.shaksternano.borgar.media.template

import io.github.shaksternano.borgar.media.AudioFrame
import io.github.shaksternano.borgar.media.ImageFrame
import io.github.shaksternano.borgar.media.graphics.Position
import io.github.shaksternano.borgar.media.graphics.TextAlignment
import io.github.shaksternano.borgar.media.graphics.drawable.Drawable
import io.github.shaksternano.borgar.media.io.MediaReaders
import io.github.shaksternano.borgar.media.io.reader.MediaReader
import java.awt.Color
import java.awt.Font
import java.awt.Shape
import java.io.InputStream
import java.net.URL
import java.util.*
import java.util.function.Function

class CustomTemplateInfo(
    val description: String,
    val mediaUrl: String,
    private val format: String,
    private val resultName: String,

    private val imageX: Int,
    private val imageY: Int,
    private val imageWidth: Int,
    private val imageHeight: Int,
    private val imagePosition: Position,

    private val textX: Int,
    private val textY: Int,
    private val textWidth: Int,
    private val textHeight: Int,
    private val textPosition: Position,
    private val textAlignment: TextAlignment,
    private val textFont: Font,
    private val textColor: Color,

    private val isBackground: Boolean,
    private val fill: Color?
) : TemplateInfo {

    override fun getImageReader(): MediaReader<ImageFrame> =
        MediaReaders.createImageReader(inputStream(), format)

    override fun getAudioReader(): MediaReader<AudioFrame> =
        MediaReaders.createAudioReader(inputStream(), format)

    private fun inputStream(): InputStream =
        URL(mediaUrl).openStream()

    override fun getFormat(): String = format

    override fun getResultName(): String = resultName

    override fun getImageContentX(): Int = imageX

    override fun getImageContentY(): Int = imageY

    override fun getImageContentWidth(): Int = imageWidth

    override fun getImageContentHeight(): Int = imageHeight

    override fun getImageContentPosition(): Position = imagePosition

    override fun getTextContentX(): Int = textX

    override fun getTextContentY(): Int = textY

    override fun getTextContentWidth(): Int = textWidth

    override fun getTextContentHeight(): Int = textHeight

    override fun getTextContentPosition(): Position = textPosition

    override fun getTextContentAlignment(): TextAlignment = textAlignment

    override fun getFont(): Font = textFont

    override fun getTextColor(): Color = textColor

    override fun getCustomTextDrawableFactory(): Optional<Function<String, Drawable>> = Optional.empty()

    override fun getContentClip(): Optional<Shape> = Optional.empty()

    override fun isBackground(): Boolean = isBackground

    override fun getFill(): Optional<Color> = Optional.ofNullable(fill)
}
