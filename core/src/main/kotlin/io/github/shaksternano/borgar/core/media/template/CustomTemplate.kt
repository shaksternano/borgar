package io.github.shaksternano.borgar.core.media.template

import io.github.shaksternano.borgar.core.graphics.ContentPosition
import io.github.shaksternano.borgar.core.graphics.TextAlignment
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.createAudioReader
import io.github.shaksternano.borgar.core.media.createImageReader
import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.supportsTransparency
import io.github.shaksternano.borgar.core.util.ChannelEnvironment
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
