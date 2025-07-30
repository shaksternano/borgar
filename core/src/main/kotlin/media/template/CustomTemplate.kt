package com.shakster.borgar.core.media.template

import com.shakster.borgar.core.graphics.ContentPosition
import com.shakster.borgar.core.graphics.TextAlignment
import com.shakster.borgar.core.graphics.drawable.Drawable
import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.media.createAudioReader
import com.shakster.borgar.core.media.createImageReader
import com.shakster.borgar.core.media.reader.AudioReader
import com.shakster.borgar.core.media.reader.ImageReader
import com.shakster.borgar.core.media.supportsTransparency
import com.shakster.borgar.core.util.ChannelEnvironment
import java.awt.Color
import java.awt.Font
import java.awt.Shape
import java.nio.file.Path
import kotlin.io.path.extension

data class CustomTemplate(
    val commandName: String,
    val entityId: String,

    val description: String,
    val entityEnvironment: ChannelEnvironment,
    val mediaPath: Path,
    override val resultName: String,

    override val imageContentX: Int,
    override val imageContentY: Int,
    override val imageContentWidth: Int,
    override val imageContentHeight: Int,
    override val imageContentPosition: ContentPosition,

    override val textContentX: Int,
    override val textContentY: Int,
    override val textContentWidth: Int,
    override val textContentHeight: Int,
    override val textContentPosition: ContentPosition,
    override val textContentAlignment: TextAlignment,

    override val font: Font,
    override val textColor: Color,
    override val contentRotationRadians: Double,
    override val isBackground: Boolean,
    override val fill: Color?
) : Template {

    override val media: DataSource = DataSource.fromFile(mediaPath)
    override val format: String = mediaPath.extension
    override val customTextDrawableSupplier: ((String) -> Drawable)? = null
    override val forceTransparency: Boolean = supportsTransparency(format)

    override suspend fun getImageReader(): ImageReader =
        createImageReader(media, format)

    override suspend fun getAudioReader(): AudioReader =
        createAudioReader(media, format)

    override suspend fun getContentClip(): Shape? = null
}
