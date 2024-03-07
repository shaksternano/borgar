package io.github.shaksternano.borgar.core.media.template

import io.github.shaksternano.borgar.core.graphics.ContentPosition
import io.github.shaksternano.borgar.core.graphics.TextAlignment
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import java.awt.Color
import java.awt.Font
import java.awt.Shape

interface Template {

    val media: DataSource
    val format: String
    val resultName: String

    val imageContentX: Int
    val imageContentY: Int
    val imageContentWidth: Int
    val imageContentHeight: Int
    val imageContentPosition: ContentPosition

    val textContentX: Int
    val textContentY: Int
    val textContentWidth: Int
    val textContentHeight: Int
    val textContentPosition: ContentPosition
    val textContentAlignment: TextAlignment

    val font: Font
    val textColor: Color
    val customTextDrawableSupplier: ((String) -> Drawable)?
    val contentRotationRadians: Double
    val isBackground: Boolean
    val fill: Color?
    val forceTransparency: Boolean

    suspend fun getImageReader(): ImageReader

    suspend fun getAudioReader(): AudioReader

    suspend fun getContentClip(): Shape?
}
