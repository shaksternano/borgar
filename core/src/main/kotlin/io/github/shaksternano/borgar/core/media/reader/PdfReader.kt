package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.io.deleteSilently
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PdfReader(
    private val pdfDocument: PDDocument,
    private val pdfRenderer: PDFRenderer,
    override val width: Int,
    override val height: Int,
    private val toDelete: Path?,
): BaseImageReader() {

    override val frameCount: Int = pdfDocument.numberOfPages
    override val frameDuration: Duration = 1.seconds
    override val duration: Duration = frameDuration * frameCount
    override val frameRate: Double = 1000.0 / frameDuration.inWholeMilliseconds
    override val loopCount: Int = 0

    override suspend fun readFrame(timestamp: Duration): ImageFrame {
        val page = (timestamp / frameDuration).toInt()
        val circularPage = page % frameCount
        val image = pdfRenderer.getImage(circularPage)
        return ImageFrame(image, frameDuration, frameDuration * circularPage)
    }

    override fun asFlow(): Flow<ImageFrame> = flow {
        for (page in 0..<frameCount) {
            val image = pdfRenderer.getImage(page)
            val frame = ImageFrame(image, frameDuration, frameDuration * page)
            emit(frame)
        }
    }

    override suspend fun close() = closeAll(
        SuspendCloseable.fromBlocking(pdfDocument),
        SuspendCloseable {
            toDelete?.deleteSilently()
        },
    )

    object Factory : ImageReaderFactory {

        override val supportedFormats: Set<String> = setOf("pdf")

        override suspend fun create(input: DataSource): ImageReader {
            val isTempFile = input.path == null
            val path = input.getOrWriteFile().path
            return withContext(Dispatchers.IO) {
                val pdfDocument = Loader.loadPDF(path.toFile())
                val renderer = PDFRenderer(pdfDocument)
                val firstImage = renderer.getImage(0)
                PdfReader(
                    pdfDocument,
                    renderer,
                    firstImage.width,
                    firstImage.height,
                    if (isTempFile) path else null
                )
            }
        }
    }
}

private const val PDF_DPI: Float = 300F

private suspend fun PDFRenderer.getImage(page: Int) = withContext(Dispatchers.IO) {
    renderImageWithDPI(page, PDF_DPI)
}
