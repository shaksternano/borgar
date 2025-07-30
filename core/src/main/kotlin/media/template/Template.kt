package com.shakster.borgar.core.media.template

import com.shakster.borgar.core.graphics.ContentPosition
import com.shakster.borgar.core.graphics.TextAlignment
import com.shakster.borgar.core.graphics.drawable.Drawable
import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.media.reader.AudioReader
import com.shakster.borgar.core.media.reader.ImageReader
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
