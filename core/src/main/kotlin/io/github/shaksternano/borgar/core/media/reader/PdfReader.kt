package io.github.shaksternano.borgar.core.media.reader

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.github.shaksternano.borgar.core.collect.getOrPut
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.io.deleteSilently
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import io.github.shaksternano.borgar.core.media.resize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.image.BufferedImage
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

    private val imageCache: Cache<Int, BufferedImage> = CacheBuilder.newBuilder()
        .maximumSize(10)
        .build()
    private val mutex: Mutex = Mutex()

    override suspend fun readFrame(timestamp: Duration): ImageFrame {
        val page = (timestamp / frameDuration).toInt()
        val circularPage = page % frameCount
        val image = getImage(circularPage)
        return ImageFrame(image, frameDuration, frameDuration * circularPage)
    }

    override fun asFlow(): Flow<ImageFrame> = flow {
        for (page in 0..<frameCount) {
            val image = getImage(page)
            val frame = ImageFrame(image, frameDuration, frameDuration * page)
            emit(frame)
        }
    }

    private suspend fun getImage(page: Int): BufferedImage =
        imageCache.getOrPut(page) {
            mutex.withLock {
                pdfRenderer.getImage(page)
            }.let {
                if (it.width != width || it.height != height) {
                    val resized = it.resize(width, height)
                    val result = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    val x = (width - resized.width) / 2
                    val y = (height - resized.height) / 2
                    val graphics = result.createGraphics()
                    graphics.drawImage(resized, x, y, null)
                    graphics.dispose()
                    result
                } else {
                    it
                }
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
                var maxWidth = 0
                var maxHeight = 0
                repeat(pdfDocument.numberOfPages) {
                    val image = renderer.getImage(it)
                    maxWidth = maxOf(maxWidth, image.width)
                    maxHeight = maxOf(maxHeight, image.height)
                }
                PdfReader(
                    pdfDocument,
                    renderer,
                    maxWidth,
                    maxHeight,
                    if (isTempFile) path else null,
                )
            }
        }
    }
}

private const val PDF_DPI: Int = 200

private suspend fun PDFRenderer.getImage(page: Int): BufferedImage =
    withContext(Dispatchers.IO) {
        renderImageWithDPI(page, PDF_DPI.toFloat())
    }
