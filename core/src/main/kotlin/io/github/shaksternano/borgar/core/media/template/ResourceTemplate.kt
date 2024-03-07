package io.github.shaksternano.borgar.core.media.template

import io.github.shaksternano.borgar.core.graphics.ContentPosition
import io.github.shaksternano.borgar.core.graphics.TextAlignment
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.fileExtension
import io.github.shaksternano.borgar.core.media.createAudioReader
import io.github.shaksternano.borgar.core.media.createImageReader
import io.github.shaksternano.borgar.core.media.reader.AudioReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Color
import java.awt.Font
import java.awt.Shape
import java.io.IOException
import java.io.ObjectInputStream

enum class ResourceTemplate(
    private val mediaPath: String,
    override val resultName: String,

    imageContainerStartX: Int,
    imageContainerStartY: Int,
    imageContainerEndX: Int,
    imageContainerEndY: Int,
    imageContainerPadding: Int,
    override val imageContentPosition: ContentPosition,

    textContainerStartX: Int,
    textContainerStartY: Int,
    textContainerEndX: Int,
    textContainerEndY: Int,
    textContainerPadding: Int,
    override val textContentPosition: ContentPosition,
    override val textContentAlignment: TextAlignment,

    fontName: String,
    maxFontSize: Int,
    override val textColor: Color,
    override val isBackground: Boolean,
    override val contentRotationRadians: Double = 0.0,
    override val fill: Color? = null,
    private val contentClipShapePath: String? = null,
    override val customTextDrawableSupplier: ((String) -> Drawable)? = null,
    override val forceTransparency: Boolean = false,
) : Template {

    SONIC_SAYS(
        mediaPath = "media/containerimage/sonic_says.png",
        resultName = "sonic_says",

        contentContainerStartX = 210,
        contentContainerStartY = 15,
        contentContainerEndX = 620,
        contentContainerEndY = 330,
        contentContainerPadding = 30,
        contentPosition = ContentPosition.CENTRE,
        textContentAlignment = TextAlignment.CENTRE,

        fontName = "Bitstream Vera Sans",
        maxFontSize = 200,
        textColor = Color.WHITE,
        isBackground = true,
        forceTransparency = true,
    ),

    SOYJAK_POINTING(
        mediaPath = "media/containerimage/soyjak_pointing.png",
        resultName = "soyjak_pointing",

        imageContainerStartX = 0,
        imageContainerStartY = 100,
        imageContainerEndX = 1024,
        imageContainerEndY = 450,
        imageContainerPadding = 0,
        imageContentPosition = ContentPosition.CENTRE,

        textContainerStartX = 250,
        textContainerStartY = 100,
        textContainerEndX = 750,
        textContainerEndY = 450,
        textContainerPadding = 0,
        textContentPosition = ContentPosition.CENTRE,
        textContentAlignment = TextAlignment.CENTRE,

        fontName = "Futura-CondensedExtraBold",
        maxFontSize = 200,
        textColor = Color.BLACK,
        isBackground = false,
        forceTransparency = true,
    ),

    THINKING_BUBBLE(
        mediaPath = "media/containerimage/thinking_bubble.png",
        resultName = "thinking_bubble",

        imageContainerStartX = 12,
        imageContainerStartY = 0,
        imageContainerEndX = 128,
        imageContainerEndY = 81,
        imageContainerPadding = 10,
        imageContentPosition = ContentPosition.CENTRE,

        textContainerStartX = 12,
        textContainerStartY = 0,
        textContainerEndX = 128,
        textContainerEndY = 81,
        textContainerPadding = 20,
        textContentPosition = ContentPosition.CENTRE,
        textContentAlignment = TextAlignment.CENTRE,

        fontName = "Futura-CondensedExtraBold",
        maxFontSize = 50,
        textColor = Color.BLACK,
        isBackground = false,
        fill = Color.WHITE,
        contentClipShapePath = "shape/thinking_bubble_edge_trimmed.javaobject",
        forceTransparency = true,
    ),

    MUTA_SOY(
        mediaPath = "media/containerimage/muta_soy.png",
        resultName = "muta_soy",

        contentContainerStartX = 400,
        contentContainerStartY = 256,
        contentContainerEndX = 800,
        contentContainerEndY = 768,
        contentContainerPadding = 0,
        contentPosition = ContentPosition.CENTRE,
        textContentAlignment = TextAlignment.CENTRE,

        fontName = "Futura-CondensedExtraBold",
        maxFontSize = 100,
        textColor = Color.WHITE,
        isBackground = false,
        forceTransparency = true,
    ),

    WALMART_WANTED(
        mediaPath = "media/containerimage/walmart_wanted.png",
        resultName = "walmart_wanted",

        imageContainerStartX = 428,
        imageContainerStartY = 94,
        imageContainerEndX = 618,
        imageContainerEndY = 318,
        imageContainerPadding = 0,
        imageContentPosition = ContentPosition.CENTRE,

        textContainerStartX = 428,
        textContainerStartY = 94,
        textContainerEndX = 618,
        textContainerEndY = 318,
        textContainerPadding = 10,
        textContentPosition = ContentPosition.CENTRE,
        textContentAlignment = TextAlignment.CENTRE,

        fontName = "Futura-CondensedExtraBold",
        maxFontSize = 200,
        textColor = Color.BLACK,
        isBackground = true,
    ),

    OH_MY_GOODNESS_GRACIOUS(
        mediaPath = "media/containerimage/oh_my_goodness_gracious.gif",
        resultName = "oh_my_goodness_gracious",

        imageContainerStartX = 250,
        imageContainerStartY = 350,
        imageContainerEndX = 536,
        imageContainerEndY = 640,
        imageContainerPadding = 0,
        imageContentPosition = ContentPosition.CENTRE,

        textContainerStartX = 250,
        textContainerStartY = 350,
        textContainerEndX = 536,
        textContainerEndY = 640,
        textContainerPadding = 10,
        textContentPosition = ContentPosition.CENTRE,
        textContentAlignment = TextAlignment.CENTRE,

        fontName = "Futura-CondensedExtraBold",
        maxFontSize = 200,
        textColor = Color.BLACK,
        isBackground = true,
    ),

    LIVING_IN_1984(
        mediaPath = "media/containerimage/living_in_1984.png",
        resultName = "living_in_1984",

        contentContainerStartX = 40,
        contentContainerStartY = 6,
        contentContainerEndX = 350,
        contentContainerEndY = 120,
        contentContainerPadding = 10,
        contentPosition = ContentPosition.CENTRE,
        textContentAlignment = TextAlignment.CENTRE,

        fontName = "Futura-CondensedExtraBold",
        maxFontSize = 100,
        textColor = Color.BLACK,
        isBackground = true,
    ),

    WHO_DID_THIS(
        mediaPath = "media/containerimage/who_did_this.png",
        resultName = "who_did_this",

        contentContainerStartX = 0,
        contentContainerStartY = 104,
        contentContainerEndX = 512,
        contentContainerEndY = 403,
        contentContainerPadding = 10,
        contentPosition = ContentPosition.CENTRE,
        textContentAlignment = TextAlignment.CENTRE,

        fontName = "Futura-CondensedExtraBold",
        maxFontSize = 100,
        textColor = Color.BLACK,
        isBackground = true,
        fill = Color.WHITE,
    ),
    ;

    constructor(
        mediaPath: String,
        resultName: String,
        contentContainerStartX: Int,
        contentContainerStartY: Int,
        contentContainerEndX: Int,
        contentContainerEndY: Int,
        contentContainerPadding: Int,
        contentPosition: ContentPosition,
        textContentAlignment: TextAlignment,
        fontName: String,
        maxFontSize: Int,
        textColor: Color,
        isBackground: Boolean,
        contentRotationRadians: Double = 0.0,
        fill: Color? = null,
        contentClipShapePath: String? = null,
        customTextDrawableSupplier: ((String) -> Drawable)? = null,
        forceTransparency: Boolean = false,
    ) : this(
        mediaPath,
        resultName,

        contentContainerStartX,
        contentContainerStartY,
        contentContainerEndX,
        contentContainerEndY,
        contentContainerPadding,
        contentPosition,

        contentContainerStartX,
        contentContainerStartY,
        contentContainerEndX,
        contentContainerEndY,
        contentContainerPadding,
        contentPosition,
        textContentAlignment,

        fontName,
        maxFontSize,
        textColor,
        isBackground,
        contentRotationRadians,
        fill,
        contentClipShapePath,
        customTextDrawableSupplier,
        forceTransparency,
    )

    override val media: DataSource = DataSource.fromResource(mediaPath)
    override val format: String = fileExtension(mediaPath)

    override val imageContentX: Int = imageContainerStartX + imageContainerPadding
    override val imageContentY: Int = imageContainerStartY + imageContainerPadding
    override val imageContentWidth: Int = imageContainerEndX - imageContainerStartX - 2 * imageContainerPadding
    override val imageContentHeight: Int = imageContainerEndY - imageContainerStartY - 2 * imageContainerPadding

    override val textContentX: Int = textContainerStartX + textContainerPadding
    override val textContentY: Int = textContainerStartY + textContainerPadding
    override val textContentWidth: Int = textContainerEndX - textContainerStartX - 2 * textContainerPadding
    override val textContentHeight: Int = textContainerEndY - textContainerStartY - 2 * textContainerPadding

    override val font: Font = Font(fontName, Font.PLAIN, maxFontSize)

    override suspend fun getImageReader(): ImageReader {
        val dataSource = DataSource.fromResource(mediaPath)
        return createImageReader(dataSource)
    }

    override suspend fun getAudioReader(): AudioReader {
        val dataSource = DataSource.fromResource(mediaPath)
        return createAudioReader(dataSource)
    }

    override suspend fun getContentClip(): Shape? {
        if (contentClipShapePath == null) return null
        val dataSource = DataSource.fromResource(contentClipShapePath)
        return runCatching {
            val inputStream = dataSource.newStream()
            val parsedObject = withContext(Dispatchers.IO) {
                ObjectInputStream(inputStream).use {
                    it.readObject()
                }
            }
            if (parsedObject is Shape) parsedObject
            else {
                var errorMessage = "The parsed object under $contentClipShapePath is not a shape"
                val parsedObjectClassName = parsedObject::class.qualifiedName
                if (parsedObjectClassName != null)
                    errorMessage += ", but a $parsedObjectClassName"
                throw IOException(errorMessage)
            }
        }.getOrElse {
            throw IOException("Failed to load shape file under $contentClipShapePath", it)
        }
    }
}
