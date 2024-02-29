package io.github.shaksternano.borgar.core.media.template

import io.github.shaksternano.borgar.core.graphics.ContentPosition
import io.github.shaksternano.borgar.core.graphics.TextAlignment
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.createAudioReader
import io.github.shaksternano.borgar.core.media.createImageReader
import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import java.awt.Color
import java.awt.Font
import java.awt.Shape

class CustomTemplate(
    val commandName: String,
    val entityId: String,

    val description: String,
    val mediaUrl: String,

    override val format: String,
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

    override val media: DataSource = DataSource.fromUrl(mediaUrl)
    override val customTextDrawableSupplier: ((String) -> Drawable)? = null

    override suspend fun getImageReader(): ImageReader {
        val dataSource = DataSource.fromUrl(mediaUrl)
        return createImageReader(dataSource, format)
    }

    override suspend fun getAudioReader(): AudioReader {
        val dataSource = DataSource.fromUrl(mediaUrl)
        return createAudioReader(dataSource, format)
    }

    override suspend fun getContentClip(): Shape? = null
}
