package io.github.shaksternano.borgar.core.media

import java.awt.*
import java.awt.image.*
import java.util.*

class DualBufferedImage(
    private val first: BufferedImage,
    val second: BufferedImage,
) : BufferedImage(
    first.width,
    first.height,
    first.type,
) {

    override fun getWidth(): Int = first.width

    override fun getWidth(observer: ImageObserver?): Int = first.getWidth(observer)

    override fun getHeight(): Int = first.height

    override fun getHeight(observer: ImageObserver?): Int = first.getHeight(observer)

    override fun getSource(): ImageProducer = first.source

    override fun getGraphics(): Graphics = first.graphics

    override fun getProperty(name: String, observer: ImageObserver?): Any = first.getProperty(name, observer)

    override fun getProperty(name: String): Any = first.getProperty(name)

    override fun getScaledInstance(width: Int, height: Int, hints: Int): Image =
        first.getScaledInstance(width, height, hints)

    override fun flush() = first.flush()

    override fun getCapabilities(gc: GraphicsConfiguration): ImageCapabilities = first.getCapabilities(gc)

    override fun setAccelerationPriority(priority: Float) = first.setAccelerationPriority(priority)

    override fun getAccelerationPriority(): Float = first.accelerationPriority

    override fun getSources(): Vector<RenderedImage> = first.sources

    override fun getPropertyNames(): Array<String> = first.propertyNames

    override fun getColorModel(): ColorModel = first.colorModel

    override fun getSampleModel(): SampleModel = first.sampleModel

    override fun getMinX(): Int = first.minX

    override fun getMinY(): Int = first.minY

    override fun getNumXTiles(): Int = first.numXTiles

    override fun getNumYTiles(): Int = first.numYTiles

    override fun getMinTileX(): Int = first.minTileX

    override fun getMinTileY(): Int = first.minTileY

    override fun getTileWidth(): Int = first.tileWidth

    override fun getTileHeight(): Int = first.tileHeight

    override fun getTileGridXOffset(): Int = first.tileGridXOffset

    override fun getTileGridYOffset(): Int = first.tileGridYOffset

    override fun getTile(tileX: Int, tileY: Int): Raster = first.getTile(tileX, tileY)

    override fun getData(): Raster = first.data

    override fun getData(rect: Rectangle): Raster = first.getData(rect)

    override fun copyData(outRaster: WritableRaster): WritableRaster = first.copyData(outRaster)

    override fun addTileObserver(to: TileObserver) = first.addTileObserver(to)

    override fun removeTileObserver(to: TileObserver) = first.removeTileObserver(to)

    override fun getWritableTile(tileX: Int, tileY: Int): WritableRaster = first.getWritableTile(tileX, tileY)

    override fun releaseWritableTile(tileX: Int, tileY: Int) = first.releaseWritableTile(tileX, tileY)

    override fun isTileWritable(tileX: Int, tileY: Int): Boolean = first.isTileWritable(tileX, tileY)

    override fun getWritableTileIndices(): Array<Point> = first.writableTileIndices

    override fun hasTileWriters(): Boolean = first.hasTileWriters()

    override fun setData(r: Raster) = first.setData(r)

    override fun getTransparency(): Int = first.transparency

    override fun getType(): Int = first.type

    override fun getRaster(): WritableRaster = first.raster

    override fun getAlphaRaster(): WritableRaster = first.alphaRaster

    override fun getRGB(x: Int, y: Int): Int = first.getRGB(x, y)

    override fun getRGB(
        startX: Int,
        startY: Int,
        w: Int,
        h: Int,
        rgbArray: IntArray,
        offset: Int,
        scansize: Int,
    ): IntArray = first.getRGB(
        startX,
        startY,
        w,
        h,
        rgbArray,
        offset,
        scansize,
    )

    override fun setRGB(x: Int, y: Int, rgb: Int) = first.setRGB(x, y, rgb)

    override fun setRGB(
        startX: Int,
        startY: Int,
        w: Int,
        h: Int,
        rgbArray: IntArray,
        offset: Int,
        scansize: Int,
    ) = first.setRGB(
        startX,
        startY,
        w,
        h,
        rgbArray,
        offset,
        scansize,
    )

    override fun createGraphics(): Graphics2D = first.createGraphics()

    override fun getSubimage(x: Int, y: Int, w: Int, h: Int): BufferedImage = first.getSubimage(x, y, w, h)

    override fun isAlphaPremultiplied(): Boolean = first.isAlphaPremultiplied

    override fun coerceData(isAlphaPremultiplied: Boolean) = first.coerceData(isAlphaPremultiplied)

    override fun equals(other: Any?): Boolean = first == other

    override fun hashCode(): Int = first.hashCode()

    override fun toString(): String = first.toString()
}
